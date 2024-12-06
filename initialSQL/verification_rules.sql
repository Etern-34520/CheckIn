create table verification_rules
(
    id                varchar(36)   not null
        primary key,
    object_name       varchar(255)  null,
    trace             varchar(255)  null,
    level             varchar(16)   null,
    order_index       int           null,
    data_values       varchar(255)  null,
    verification_type varchar(1024) null,
    tip_template      text          null,
    target_input_name varchar(255)  null
);