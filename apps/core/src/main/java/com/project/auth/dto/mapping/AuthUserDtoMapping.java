package com.project.auth.dto.mapping;

import com.project.auth.SessionUser;
import com.project.auth.dto.AuthUserDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingInheritanceStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_ALL_FROM_CONFIG, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthUserDtoMapping {
    AuthUserDtoMapping INSTANCE = Mappers.getMapper(AuthUserDtoMapping.class);

    SessionUser toSessionUser(AuthUserDto.Authentication authUser);
}
