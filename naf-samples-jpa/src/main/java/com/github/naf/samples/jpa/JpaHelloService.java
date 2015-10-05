package com.github.naf.samples.jpa;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;

@Dependent
public class JpaHelloService {
	static int id = 0;

	@Transactional
	public void hello() {
		// entityManager.clear();

		System.err.println("hello, world\nentityManager=" + entityManager + " " + entityManager.getClass());
		for (Object o : entityManager.createQuery("from Samples").getResultList())
			System.err.println("o1 " + o);
		Samples s = new Samples();
		s.key = "key" + ++id;
		s.value = "value";
		entityManager.persist(s);
		for (Object o : entityManager.createQuery("from Samples").getResultList())
			System.err.println("o2 " + o);
	}

	@Transactional
	public void hello2() {
		System.err.println("hello2, world\nentityManager=" + entityManager + "  " + entityManager.getClass());
		for (Object o : entityManager.createQuery("from Samples").getResultList())
			System.err.println("o1 " + o);
		Samples s = new Samples();
		s.key = "key" + ++id;
		s.value = "value1";
		entityManager.persist(s);
		for (Object o : entityManager.createQuery("from Samples").getResultList())
			System.err.println("o2 " + o);

	}

	@Inject
	UserTransaction ut;

	@PersistenceUnit(unitName = "test")
	EntityManagerFactory emf;

	@PersistenceContext(unitName = "test")
	EntityManager entityManager;
}
