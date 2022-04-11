package com.cocoon.exception;

import com.cocoon.enums.InputConstraint;

public class InputLengthExceededException extends RuntimeException {
    public InputLengthExceededException(){
        super("Input length exceeded.");
    }

    public InputLengthExceededException(String inputName, int maxLength){
        super(inputName + " length exceeded. Max length is " + maxLength);
    }

    public InputLengthExceededException(InputConstraint inputConstraint){
        super(inputConstraint.getInputName() + " length exceeded. Max length is " + inputConstraint.getMaxLength());
    }
}
