package dq.platform.jpa;

public interface DataSourceFactory {
	Builder create();

	interface Builder {
		Builder setClassName(String className);

		Builder setUniqueName(String uniqueName);

		Builder setPoolSize(int min, int max);

		Builder addProperty(String key, String value);

		Builder setAllowLocalTransactions(boolean allowLocalTransaction);
	}
}
