create table role
(
    type varchar(255) not null
        primary key
);

INSERT INTO checkin.role (type) VALUES ('admin');
INSERT INTO checkin.role (type) VALUES ('advanced user');
INSERT INTO checkin.role (type) VALUES ('super admin');
INSERT INTO checkin.role (type) VALUES ('user');