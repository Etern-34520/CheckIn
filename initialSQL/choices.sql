create table choices
(
    id          varchar(255) not null
        primary key,
    content     varchar(255) null,
    is_correct  bit          null,
    question_id varchar(255) null,
    order_index int          not null
);