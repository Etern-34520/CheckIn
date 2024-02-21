create table multi_partitionable_questions
(
    dtype              varchar(31)  not null,
    id                 varchar(255) not null
        primary key,
    content            varchar(255) null,
    last_edit_time     datetime(6)  null,
    enabled            bit          not null,
    image_path_strings varchar(255) null,
    author_qqnumber    bigint       null
);

