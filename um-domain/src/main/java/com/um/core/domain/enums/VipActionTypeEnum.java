package com.um.core.domain.enums;

public enum VipActionTypeEnum {
    SUBSCRIBE(1),
    RENEW(2),
    UPGRADE(3),
    GIFT(4);

    private final int code;

    VipActionTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
