package com.receiptmate.common.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ClientIpResolver {

    public String resolve(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();

        if (!StringUtils.hasText(ipAddress)) {
            throw new IllegalStateException("클라이언트 IP 주소를 확인할 수 없습니다.");
        }

        return ipAddress;
    }

}
