package com.example.androidmidterm.UserFeature;

import androidx.annotation.NonNull;

public enum USER_ROLE {
    ADMIN,
    MANAGER,
    EMPLOYEE;

    @NonNull
    @Override
    public String toString() {
        switch (this) {
            case ADMIN:
                return "ADMIN";
            case MANAGER:
                return "MANAGER";
            case EMPLOYEE:
                return "EMPLOYEE";
            default:
                return "";
        }
    }
}
