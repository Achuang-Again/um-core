package com.um.core.auth.application;

import com.um.core.infrastructure.redis.SmsCodeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 发送短信验证码（开发环境日志输出，生产应对接网关）。
 */
@Service
@RequiredArgsConstructor
public class SmsCodeApplicationService {

    private static final Logger log = LoggerFactory.getLogger(SmsCodeApplicationService.class);

    private final SmsCodeService smsCodeService;

    public void sendLoginCode(String phone) {
        String code = smsCodeService.generateAndStore(phone);
        log.info("SMS code generated for phone={} (dev only, do not log in prod): {}", maskPhone(phone), code);
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
