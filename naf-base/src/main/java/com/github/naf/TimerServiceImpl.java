package com.github.naf;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

import javax.ejb.EJBException;
import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.manager.BeanManagerImpl;

public class TimerServiceImpl implements TimerService {

	private InjectionPoint injectionPoint;
	private BeanManagerImpl bm;

	public TimerServiceImpl(InjectionPoint injectionPoint, BeanManagerImpl bm) {
		Objects.requireNonNull(injectionPoint);
		this.injectionPoint = injectionPoint;
		Objects.requireNonNull(bm);
		this.bm = bm;
	}

	@Override
	public Timer createCalendarTimer(ScheduleExpression schedule)
			throws IllegalArgumentException, IllegalStateException, EJBException {
		throw new Error();
	}

	@Override
	public Timer createCalendarTimer(ScheduleExpression schedule, TimerConfig timerConfig)
			throws IllegalArgumentException, IllegalStateException, EJBException {
		throw new Error();
	}

	@Override
	public Timer createIntervalTimer(Date initialExpiration, long intervalDuration, TimerConfig timerConfig)
			throws IllegalArgumentException, IllegalStateException, EJBException {
		throw new Error();
	}

	@Override
	public Timer createIntervalTimer(long initialDuration, long intervalDuration, TimerConfig timerConfig)
			throws IllegalArgumentException, IllegalStateException, EJBException {
		throw new Error();
	}

	@Override
	public Timer createSingleActionTimer(Date expiration, TimerConfig timerConfig)
			throws IllegalArgumentException, IllegalStateException, EJBException {
		throw new Error();
	}

	@Override
	public Timer createSingleActionTimer(long duration, TimerConfig timerConfig)
			throws IllegalArgumentException, IllegalStateException, EJBException {
		throw new Error();
	}

	@Override
	public Timer createTimer(long duration, Serializable info)
			throws IllegalArgumentException, IllegalStateException, EJBException {

		throw new Error("ip " + injectionPoint + " bm=" + bm);
	}

	@Override
	public Timer createTimer(long initialDuration, long intervalDuration, Serializable info)
			throws IllegalArgumentException, IllegalStateException, EJBException {
		throw new Error();
	}

	@Override
	public Timer createTimer(Date expiration, Serializable info)
			throws IllegalArgumentException, IllegalStateException, EJBException {
		throw new Error();
	}

	@Override
	public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info)
			throws IllegalArgumentException, IllegalStateException, EJBException {
		throw new Error();
	}

	@Override
	public Collection<Timer> getTimers() throws IllegalStateException, EJBException {
		throw new Error();
	}

	@Override
	public Collection<Timer> getAllTimers() throws IllegalStateException, EJBException {
		throw new Error();
	}

}
