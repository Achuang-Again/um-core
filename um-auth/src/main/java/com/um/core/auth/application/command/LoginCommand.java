package com.um.core.auth.application.command;

public record LoginCommand(String principal, String password, String deviceId, String clientIp) {
}
