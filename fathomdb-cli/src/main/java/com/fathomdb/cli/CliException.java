package com.fathomdb.cli;

/**
 * An exception that should just output the error message (without the stack trace)
 * 
 */
public class CliException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CliException(String message, Throwable cause) {
		super(message, cause);
	}

	public CliException(String message) {
		super(message);
	}

}
