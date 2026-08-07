package com.bondtradex.ioi.downstream.model;

public enum ErrorCategory {

    TRANSIENT_INFRASTRUCTURE_ERROR,

    VALIDATION_ERROR,

    DEPENDENCY_ERROR,

    DESERIALIZATION_ERROR,

    AUTHORIZATION_ERROR,

    UNKNOWN_ERROR
}
