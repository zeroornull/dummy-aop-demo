package org.springframework.core.exception;

public class AopConfigException extends RuntimeException {

	public AopConfigException(String msg) {
		super(msg);
	}

	public AopConfigException(String msg, Throwable cause) {
		super(msg, cause);
	}

}