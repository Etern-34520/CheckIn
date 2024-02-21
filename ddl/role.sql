create table role
(
    type varchar(255) not null
        primary key
);

INSERT INTO checkIn.role (type) VALUES ('admin');
INSERT INTO checkIn.role (type) VALUES ('advanced user');
INSERT INTO checkIn.role (type) VALUES ('super admin');
INSERT INTO checkIn.role (type) VALUES ('user');
