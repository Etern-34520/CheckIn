create table partitions_questions_mapping
(
    question_id  varchar(255) not null,
    partition_id int          not null,
    primary key (question_id, partition_id)
);

create index FKlmojk4rsauadbhyr33v96f7kx
    on partitions_questions_mapping (partition_id);

