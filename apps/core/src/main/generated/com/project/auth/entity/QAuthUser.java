package com.project.auth.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAuthUser is a Querydsl query type for AuthUser
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAuthUser extends EntityPathBase<AuthUser> {

    private static final long serialVersionUID = -57062374L;

    public static final QAuthUser authUser = new QAuthUser("authUser");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final StringPath loginId = createString("loginId");

    public final StringPath password = createString("password");

    public final EnumPath<AuthUser.UserStatus> status = createEnum("status", AuthUser.UserStatus.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final StringPath userId = createString("userId");

    public final StringPath userName = createString("userName");

    public QAuthUser(String variable) {
        super(AuthUser.class, forVariable(variable));
    }

    public QAuthUser(Path<? extends AuthUser> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAuthUser(PathMetadata metadata) {
        super(AuthUser.class, metadata);
    }

}

