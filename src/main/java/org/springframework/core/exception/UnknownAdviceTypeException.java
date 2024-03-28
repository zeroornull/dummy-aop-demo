package org.springframework.core.exception;

public class UnknownAdviceTypeException extends IllegalArgumentException {

    public UnknownAdviceTypeException(Object advice) {
        super("Advice object [" + advice + "] is neither a supported subInterface of " +
                "[org.aopalliance.aop.Advice] nor an [org.springframework.aop.Advisor]");
    }

    public UnknownAdviceTypeException(String message) {
        super(message);
    }
}
