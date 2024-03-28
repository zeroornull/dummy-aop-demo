package org.springframework.core.exception;

public class AopInvocationException extends RuntimeException {

	public AopInvocationException(String msg) {
		super(msg);
	}

	public AopInvocationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
