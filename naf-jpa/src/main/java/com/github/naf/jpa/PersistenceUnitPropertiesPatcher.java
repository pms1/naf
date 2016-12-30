package com.github.naf.jpa;

import java.util.Map;
import java.util.function.BiFunction;

public interface PersistenceUnitPropertiesPatcher extends BiFunction<String, Map<String, Object>, Map<String, Object>> {

}
