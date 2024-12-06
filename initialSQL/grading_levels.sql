create table grading_levels
(
    id          varchar(36)   not null,
    name        varchar(1024) not null,
    color_hex   varchar(8)    null,
    description text          null,
    message     mediumtext    null
);