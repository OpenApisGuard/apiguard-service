package org.apiguard.service.exceptions;

public class ApiAuthException extends Exception {
	public ApiAuthException(String message) {
		super(message);
	}

	public ApiAuthException(String message, Throwable e) {
		super(message, e);
	}
}
