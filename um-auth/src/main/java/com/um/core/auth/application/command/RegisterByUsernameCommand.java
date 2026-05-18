package com.um.core.auth.application.command;

public record RegisterByUsernameCommand(String username, String password, String clientIp) {
}
