package com.project.auth.dto.mapping;

import com.project.auth.SessionUser;
import com.project.auth.dto.AuthUserDto;
import com.project.auth.entity.AuthUser;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_ALL_FROM_CONFIG, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthUserDtoMapping {
    AuthUserDtoMapping INSTANCE = Mappers.getMapper(AuthUserDtoMapping.class);

    SessionUser toSessionUser(AuthUserDto.Authentication authUser);

    @Mapping(target = "tokenId", expression = "java(com.github.f4b6a3.ulid.UlidCreator.getMonotonicUlid().toString())")
    AuthUserDto.Authentication toAuthentication(AuthUser authUser, String device, String deviceId);
}
