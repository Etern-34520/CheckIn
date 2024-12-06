create table role
(
    type  varchar(255) not null
        primary key,
    level int          null
);

INSERT INTO check_in.role (type, level) VALUES ('admin', 1);
INSERT INTO check_in.role (type, level) VALUES ('advanced user', 2);
INSERT INTO check_in.role (type, level) VALUES ('super admin', 0);
INSERT INTO check_in.role (type, level) VALUES ('user', 3);