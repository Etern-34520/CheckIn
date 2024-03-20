create table users
(
    qqnumber  bigint       not null
        primary key,
    enabled   bit          not null,
    name      varchar(255) null,
    password  varchar(255) null,
    role_type varchar(255) null
);

create index FK5djdw2lhplctx421luvv17icu
    on users (role_type);

INSERT INTO checkin.users (qqnumber, enabled, name, password, role_type) VALUES (10000, true, 'admin', '$2a$10$sKVejjBvyTv7eNI8uA3pJuEjh5Enz96MMP2aW/fZ0wlObbjIrgrmq', 'super admin');