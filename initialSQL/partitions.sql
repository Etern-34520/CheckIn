create table partitions
(
    id   int          not null
        primary key,
    name varchar(191) not null,
    constraint UK_mxwdiv3wyr9b440567a4d80m2
        unique (name)
);
