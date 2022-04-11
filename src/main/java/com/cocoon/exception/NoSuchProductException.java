package com.cocoon.exception;

public class NoSuchProductException extends RuntimeException{
    public NoSuchProductException(){
        super("No such product.");
    }
    public NoSuchProductException(Long id){
        super("There is no product belongs to id " + id);
    }
    public NoSuchProductException(String message) {
        super(message);
    }
}
