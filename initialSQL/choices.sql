create table choices
(
    id          varchar(255) not null
        primary key,
    content     text         null,
    is_correct  bit          null,
    question_id varchar(255) null
);