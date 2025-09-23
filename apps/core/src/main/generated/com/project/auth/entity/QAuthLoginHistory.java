package com.project.auth.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAuthLoginHistory is a Querydsl query type for AuthLoginHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAuthLoginHistory extends EntityPathBase<AuthLoginHistory> {

    private static final long serialVersionUID = 169063994L;

    public static final QAuthLoginHistory authLoginHistory = new QAuthLoginHistory("authLoginHistory");

    public final StringPath device = createString("device");

    public final StringPath deviceId = createString("deviceId");

    public final StringPath id = createString("id");

    public final DateTimePath<java.time.LocalDateTime> loginDate = createDateTime("loginDate", java.time.LocalDateTime.class);

    public final StringPath loginIp = createString("loginIp");

    public final DateTimePath<java.time.LocalDateTime> logoutDate = createDateTime("logoutDate", java.time.LocalDateTime.class);

    public final StringPath tokenId = createString("tokenId");

    public final StringPath userId = createString("userId");

    public QAuthLoginHistory(String variable) {
        super(AuthLoginHistory.class, forVariable(variable));
    }

    public QAuthLoginHistory(Path<? extends AuthLoginHistory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAuthLoginHistory(PathMetadata metadata) {
        super(AuthLoginHistory.class, metadata);
    }

}

