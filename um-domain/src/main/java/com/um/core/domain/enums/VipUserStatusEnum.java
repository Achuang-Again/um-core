package com.um.core.domain.enums;

public enum VipUserStatusEnum {
    EXPIRED(0),
    ACTIVE(1);

    private final int code;

    VipUserStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
