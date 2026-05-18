package com.um.core.common.constant;

/**
 * 统一错误码（分段：1 通用 / 2 认证 / 3 资料 / 4 VIP）。
 */
public final class ErrorCodes {

    public static final int SUCCESS = 0;
    public static final int SYSTEM_ERROR = 10000;
    public static final int PARAM_INVALID = 10001;
    public static final int NOT_FOUND = 10004;

    public static final int AUTH_INVALID_CREDENTIALS = 20001;
    public static final int AUTH_ACCOUNT_LOCKED = 20002;
    public static final int AUTH_USERNAME_EXISTS = 20003;
    public static final int AUTH_PHONE_EXISTS = 20004;
    public static final int AUTH_EMAIL_EXISTS = 20005;
    public static final int AUTH_INVALID_SMS_CODE = 20006;
    public static final int AUTH_ACCOUNT_DEACTIVATED = 20007;
    public static final int AUTH_GEO_RISK = 20008;
    public static final int AUTH_TOKEN_INVALID = 20010;

    public static final int PROFILE_NOT_FOUND = 30001;
    public static final int PROFILE_VERSION_CONFLICT = 30002;

    public static final int VIP_LEVEL_NOT_FOUND = 40001;
    public static final int VIP_ORDER_DUPLICATE = 40002;
    public static final int VIP_INVALID_UPGRADE = 40003;

    private ErrorCodes() {
    }
}
