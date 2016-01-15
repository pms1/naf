package com.github.naf;

import java.lang.annotation.Annotation;

public interface Application extends AutoCloseable {

	@Override
	void close();

	<T> T get(Class<T> clazz, Annotation... annotations);

	<T> Iterable<T> getAll(Class<T> clazz, Annotation... annotations);

	void join();

	void withRequestContext(Runnable runnable);
}
