package com.um.core.auth.application.command;

public record RegisterByPhoneCommand(String phone, String smsCode, String password, String clientIp) {
}
