package com.github.naf.spi;

public interface Resource {
	Object getValue();

	default void close() {

	}
}
