package com.projeto.raizesnordeste.presentation.exceptions;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class ValidationException extends StandardError {

    public void addError(final String fieldName, final String message) {
        this.getDetails().add(fieldName + ": " + message);
    }

}
