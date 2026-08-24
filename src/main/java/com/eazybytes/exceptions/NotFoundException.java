package com.eazybytes.exceptions;

public class NotFoundException extends RuntimeException{
    public NotFoundException(String entity) {
        super(entity + " " + "Not found");
    }
}