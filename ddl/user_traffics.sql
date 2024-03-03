create table user_traffics (
    id         int auto_increment
        primary key,
    attributes text         null,
    header     text         null,
    ip         varchar(255) null,
    date       date         null,
    time       time(6)      null,
    qq_number  bigint       null
);

create index FKagnavjvhptp82o54ydyi0vhhk
    on user_traffics (date);

