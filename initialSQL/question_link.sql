create table question_link
(
    id          varchar(36)  not null,
    dtype       varchar(255) not null,
    target_id   varchar(36)  null,
    order_index int          null
);