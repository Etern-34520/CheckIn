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

INSERT INTO check_in.users (qqnumber, enabled, name, password, role_type) VALUES (114514, true, '田所浩二', '$2a$10$Ve6WIqVRVN5zPXzMssxE0.cyD6XuTa2PMwOss9q7QC/RrAtWXit1C', 'super admin');