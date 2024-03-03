create table permission_groups (
    name        varchar(255) not null
        primary key,
    description varchar(255) null
);

INSERT INTO checkIn.permission_groups (name, description) VALUES ('manage user', '管理用户');
INSERT INTO checkIn.permission_groups (name, description) VALUES ('question', '题目');
INSERT INTO checkIn.permission_groups (name, description) VALUES ('role', '管理用户组');
INSERT INTO checkIn.permission_groups (name, description) VALUES ('setting', '修改设置');
