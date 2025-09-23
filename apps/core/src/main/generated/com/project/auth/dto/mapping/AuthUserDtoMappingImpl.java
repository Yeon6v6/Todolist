package com.project.auth.dto.mapping;

import com.project.auth.SessionUser;
import com.project.auth.dto.AuthUserDto;
import com.project.auth.entity.AuthUser;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-23T12:22:41+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class AuthUserDtoMappingImpl implements AuthUserDtoMapping {

    @Override
    public SessionUser toSessionUser(AuthUserDto.Authentication authUser) {
        if ( authUser == null ) {
            return null;
        }

        SessionUser.SessionUserBuilder sessionUser = SessionUser.builder();

        sessionUser.userId( authUser.getUserId() );
        sessionUser.loginId( authUser.getLoginId() );
        sessionUser.device( authUser.getDevice() );
        sessionUser.deviceId( authUser.getDeviceId() );

        return sessionUser.build();
    }

    @Override
    public AuthUserDto.Authentication toAuthentication(AuthUser authUser, String device, String deviceId) {
        if ( authUser == null && device == null && deviceId == null ) {
            return null;
        }

        AuthUserDto.Authentication.AuthenticationBuilder authentication = AuthUserDto.Authentication.builder();

        if ( authUser != null ) {
            authentication.userId( authUser.getUserId() );
            authentication.loginId( authUser.getLoginId() );
        }
        authentication.device( device );
        authentication.deviceId( deviceId );
        authentication.tokenId( com.github.f4b6a3.ulid.UlidCreator.getMonotonicUlid().toString() );

        return authentication.build();
    }
}
