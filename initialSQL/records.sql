create table records
(
    id          varchar(255) not null
        primary key,
    session_id  varchar(255) not null,
    record_time datetime     not null,
    qq          bigint       null,
    ip_string   varchar(255) not null,
    header      text         null,
    attributes  text         null,
    extra_data  text         null,
    type        int          not null
);