package com.cocoon.exception;

public class CategoryDoesNotExistException extends RuntimeException {
    public CategoryDoesNotExistException(){
        super("Category does not exist.");
    }
}
