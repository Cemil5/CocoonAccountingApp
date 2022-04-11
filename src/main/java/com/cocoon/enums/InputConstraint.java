package com.cocoon.enums;

import lombok.Getter;

@Getter
public enum InputConstraint {
    ADDRESS_INPUT("Address", 255);

    private int maxLength;
    private String inputName;

    InputConstraint(String inputName, int maxLength){
        this.maxLength = maxLength;
        this.inputName = inputName;
    }
}
