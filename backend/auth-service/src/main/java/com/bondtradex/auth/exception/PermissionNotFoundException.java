package com.bondtradex.auth.exception;

public class PermissionNotFoundException extends RuntimeException{

    public PermissionNotFoundException(String permission){
        super("Permission not found : "+permission);
    }
}
