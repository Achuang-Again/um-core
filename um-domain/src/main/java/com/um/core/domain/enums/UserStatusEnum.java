package com.um.core.domain.enums;

public enum UserStatusEnum {
    DISABLED(0),
    NORMAL(1),
    DEACTIVATED(2);

    private final int code;

    UserStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static UserStatusEnum fromCode(int code) {
        for (UserStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown user status: " + code);
    }
}
