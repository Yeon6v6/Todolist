package com.project.auth;

import com.project.common.CommonConstants;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class SessionUser {
    private String userId;
    private String loginId;
    private String device;
    private String deviceId;    // 기기 고유 값

    public boolean isPC() {
        return CommonConstants.DEVICE_PC.equals(device);
    }

    public boolean isMobile() {
        return CommonConstants.DEVICE_MOBILE.equals(device);
    }
}
