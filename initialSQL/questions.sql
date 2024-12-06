create table questions
(
    dtype                varchar(31) not null,
    id                   varchar(36) not null
        primary key,
    content              text        null,
    last_modified_time   datetime(6) null,
    enabled              bit         not null,
    image_base64_strings mediumblob  null,
    author_qqnumber      bigint      null,
    sub_type             tinyint     null,
    random_ordered       tinyint(1)  null
);