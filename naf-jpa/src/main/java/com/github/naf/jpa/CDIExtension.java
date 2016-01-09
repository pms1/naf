package com.github.naf.jpa;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceProperty;
import javax.persistence.PersistenceUnit;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.hibernate.engine.spi.SessionImplementor;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.injection.spi.JpaInjectionServices;
import org.jboss.weld.injection.spi.ResourceReference;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;
import org.jboss.weld.transaction.spi.TransactionServices;

import com.github.naf.spi.ApplicationContext;
import com.github.naf.spi.RequirementException;

public class CDIExtension implements com.github.naf.spi.Extension {

	public static ThreadLocal<ApplicationContext> applicationContext = new ThreadLocal<>();

	static class Ref {
		final AtomicInteger references = new AtomicInteger(1);
		final EntityManagerFactory emf;

		Ref(EntityManagerFactory emf) {
			this.emf = emf;
		}
	}

	private Map<Id, Ref> entityManagers = new HashMap<>();

	EntityManagerFactory create(Id id) {
		synchronized (this) {
			Ref result = entityManagers.get(id);
			if (result == null) {
				EntityManagerFactory emf;

				if (applicationContext.get() != null)
					throw new IllegalStateException();
				try {
					applicationContext.set(ac);
					if (ts.isTransactionActive())
						throw new Error();
					boolean doRollback = false;
					try {
						try {
							ts.getUserTransaction().begin();
							doRollback = true;
							if (id.properties != null)
								emf = Persistence.createEntityManagerFactory(id.unitName, id.properties);
							else
								emf = Persistence.createEntityManagerFactory(id.unitName);
							ts.getUserTransaction().commit();
							doRollback = false;
						} finally {
							if (doRollback)
								ts.getUserTransaction().rollback();
						}
					} catch (SystemException | NotSupportedException | RollbackException | HeuristicRollbackException
							| HeuristicMixedException e) {
						throw new Error(e);
					}
				} finally {
					applicationContext.remove();
				}

				result = new Ref(emf);
				Object old = entityManagers.put(id, result);
				if (old != null)
					throw new Error();
			} else {
				result.references.incrementAndGet();
			}
			return result.emf;
		}
	}

	void release(Id id) {
		synchronized (this) {
			Ref ref = entityManagers.get(id);

			if (ref.references.decrementAndGet() == 0) {
				entityManagers.remove(id);
				ref.emf.close();
			}
		}
	}

	private ApplicationContext ac;
	private TransactionServices ts;

	@Override
	public Deployment processDeployment(ApplicationContext ac, Deployment deployment) throws RequirementException {
		this.ac = ac;

		TransactionServices services = deployment.getServices().get(TransactionServices.class);
		if (services == null)
			throw new RequirementException();
		this.ts = services;

		deployment.getServices().add(JpaInjectionServices.class, new JpaInjectionServices() {

			@Override
			public void cleanup() {
				synchronized (CDIExtension.this) {
					entityManagers.values().forEach(ref -> ref.emf.close());
					entityManagers.clear();

				}
			}

			@Override
			public ResourceReferenceFactory<EntityManager> registerPersistenceContextInjectionPoint(
					InjectionPoint injectionPoint) {
				PersistenceContext pc = injectionPoint.getAnnotated().getAnnotation(PersistenceContext.class);
				if (pc == null)
					throw new IllegalArgumentException();
				if (!pc.name().equals(""))
					throw new UnsupportedOperationException("not supported (yet)");
				if (pc.unitName().equals(""))
					throw new UnsupportedOperationException("not supported (yet)");
				if (pc.type() == PersistenceContextType.EXTENDED)
					throw new UnsupportedOperationException("not supported (yet)");
				Map<String, String> properties = asMap(pc.properties());

				Id id = new Id(pc.unitName(), properties);
				return new ResourceReferenceFactory<EntityManager>() {

					@Override
					public ResourceReference<EntityManager> createResource() {
						return new ResourceReference<EntityManager>() {

							EntityManagerFactory created;

							@Override
							public EntityManager getInstance() {
								created = create(id);

								EntityManager em;
								if (properties != null)
									em = created.createEntityManager(pc.synchronization(), properties);
								else
									em = created.createEntityManager(pc.synchronization());

								// make sure that the session is cleared on
								// commit()
								SessionImplementor s = em.unwrap(SessionImplementor.class);
								s.setAutoClear(true);

								return em;
							}

							@Override
							public void release() {
								if (created != null) {
									created = null;
									CDIExtension.this.release(id);
								}
							}
						};
					}
				};
			}

			@Override
			public ResourceReferenceFactory<EntityManagerFactory> registerPersistenceUnitInjectionPoint(
					InjectionPoint injectionPoint) {
				System.err.print("XX3 " + injectionPoint);
				PersistenceUnit pc = injectionPoint.getAnnotated().getAnnotation(PersistenceUnit.class);
				if (pc == null)
					throw new IllegalArgumentException();
				Id id = new Id(pc.unitName(), null);
				return new ResourceReferenceFactory<EntityManagerFactory>() {

					@Override
					public ResourceReference<EntityManagerFactory> createResource() {

						return new ResourceReference<EntityManagerFactory>() {

							EntityManagerFactory created;

							@Override
							public EntityManagerFactory getInstance() {
								if (created != null)
									throw new Error();

								return create(id);
							}

							@Override
							public void release() {
								if (created != null) {
									created = null;
									CDIExtension.this.release(id);
								}

							}
						};
					}
				};
			}

			@Override
			public EntityManager resolvePersistenceContext(InjectionPoint injectionPoint) {
				System.err.print("XX4 " + injectionPoint);
				return null;
			}

			@Override
			public javax.persistence.EntityManagerFactory resolvePersistenceUnit(InjectionPoint injectionPoint) {
				System.err.print("XX5 " + injectionPoint);
				return null;
			}

		});

		return com.github.naf.spi.Extension.super.processDeployment(ac, deployment);
	}

	private static Map<String, String> asMap(PersistenceProperty[] properties) {
		if (properties.length == 0)
			return null;

		Map<String, String> result = new HashMap<>();
		for (PersistenceProperty p : properties)
			result.put(p.name(), p.value());
		return result;
	}

	static class Id {
		final String unitName;
		final Map<String, String> properties;

		public Id(String unitName, Map<String, String> properties) {
			this.unitName = unitName;
			this.properties = properties;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((properties == null) ? 0 : properties.hashCode());
			result = prime * result + ((unitName == null) ? 0 : unitName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Id other = (Id) obj;
			if (properties == null) {
				if (other.properties != null)
					return false;
			} else if (!properties.equals(other.properties))
				return false;
			if (unitName == null) {
				if (other.unitName != null)
					return false;
			} else if (!unitName.equals(other.unitName))
				return false;
			return true;
		}

	}
}
