package com.github.naf.spi;

/**
 * Thrown by {@link Extension#processDeployment} to indicate that not all
 * requirements are met.
 * 
 * @author pms1
 *
 */
public class RequirementException extends Exception {
	public RequirementException(String message) {
		super(message);
	}
}
