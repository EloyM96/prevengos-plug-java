package com.prevengos.plug.shared.validation;

/**
 * Exception thrown when an entity built from a shared contract violates one of the
 * validation rules derived from the JSON Schema definitions.
 */
public final class ContractValidationException extends RuntimeException {

    public ContractValidationException(String message) {
        super(message);
    }

    public ContractValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
