create table questions_link_mapping
(
    question_id  varchar(36) not null,
    partition_id int         not null,
    primary key (question_id, partition_id)
);

create index FKlmojk4rsauadbhyr33v96f7kx
    on questions_link_mapping (partition_id);