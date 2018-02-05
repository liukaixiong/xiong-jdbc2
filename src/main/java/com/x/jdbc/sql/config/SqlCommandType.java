package com.x.jdbc.sql.config;

public enum SqlCommandType {

    UNKNOWN,
    INSERT,
    UPDATE,
    DELETE,
    SELECT;

    private SqlCommandType() {
    }
}