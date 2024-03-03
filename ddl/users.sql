create table users (
    qqnumber  bigint       not null
        primary key,
    enabled   bit          not null,
    name      varchar(255) null,
    password  varchar(255) null,
    role_type varchar(255) null
);

create index FK5djdw2lhplctx421luvv17icu
    on users (role_type);

INSERT INTO checkIn.users (qqnumber, enabled, name, password, role_type) VALUES (114514, true, '田所浩二', '$2a$10$Ve6WIqVRVN5zPXzMssxE0.cyD6XuTa2PMwOss9q7QC/RrAtWXit1C', 'advanced user');
INSERT INTO checkIn.users (qqnumber, enabled, name, password, role_type) VALUES (941651914, true, 'etern', '$2a$10$Q2xVSf/rxzZFX2sLxfXEwuEPdnoFWId1x.c0ERrcI.1w8MHXvm3Vi', 'super admin');
INSERT INTO checkIn.users (qqnumber, enabled, name, password, role_type) VALUES (2737914384, true, 'harker best', '$2a$10$vMpW7IkFj5ehNkPrFxwtd.TDnProCw.vdTLtoLgNzBynNA7TOZ5ZS', 'super admin');
INSERT INTO checkIn.users (qqnumber, enabled, name, password, role_type) VALUES (2797512412, true, 'cheeseBear', '$2a$10$vMpW7IkFj5ehNkPrFxwtd.TDnProCw.vdTLtoLgNzBynNA7TOZ5ZS', 'super admin');
INSERT INTO checkIn.users (qqnumber, enabled, name, password, role_type) VALUES (3221520688, true, '不吃胡萝卜の兔子', '$2a$10$7.1fAqT6365nNJrBrKli/e.vrklM.E7qlu7Jz9d5dzYTNaSNKSo3W', 'super admin');
INSERT INTO checkIn.users (qqnumber, enabled, name, password, role_type) VALUES (3363880992, true, '钉佬', '$2a$10$vMpW7IkFj5ehNkPrFxwtd.TDnProCw.vdTLtoLgNzBynNA7TOZ5ZS', 'super admin');
