create table permissions
(
    id          varchar(36)  not null
        primary key,
    description varchar(255) null,
    name        varchar(255) not null,
    group_name  varchar(255) null
);

create index FKh66mayksvrngibtfbh6a7acds
    on permissions (group_name);

INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('02c72178-bac2-424c-aa38-1c60a2aa1d6e', '添加用户', 'create user', 'manage user');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('07a13959-7044-4f7c-bf4c-661853871700', '修改用户所属组组到：admin', 'change role admin', 'role');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('0dd329a2-227d-4980-a951-2789d0373226', '强制下线用户', 'force offline', 'manage user');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('186c3767-02af-48b8-84a1-24cdabc19961', '启/禁用用户', 'change user state', 'manage user');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('18a698c5-199a-4ee6-be94-3a9b2b712c59', '修改首頁設置', 'save setting facadeSetting', 'setting');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('2c9e517f-6edf-4cc9-a5e6-a582a87c3753', '删除分区', 'delete partition', 'question');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('2e23a0dc-ecc2-448e-92d5-09d698553d31', '创建分区', 'create partition', 'question');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('4be6129d-b3b0-41df-9fcc-946ab36545dc', '编辑分区名称', 'edit partition name', 'question');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('4d0be824-2e7b-48fa-aba3-a1a37c452e23', '启/禁用题目', 'enable and disable question', 'question');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('58bed2fa-ca97-411c-b511-e7bf31a435cc', '修改用户组权限', 'edit permission', 'role');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('67b26d13-8373-4264-ba2a-4d8201ceebb0', '修改用户所属组组到：super admin', 'change role super admin', 'role');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('6aa6faef-510b-41b6-a4bc-6ee51a8d19a6', '删除用户组', 'delete role', 'role');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('837B1E7D-4F84-4A21-B921-87EB6936F716', '修改答题设置', 'save setting examSetting', 'setting');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('8f8e65b1-968b-4e47-a26f-466ab6e095b8', '删除用户', 'delete user', 'manage user');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('9809102d-91a5-4b6e-8a31-1f2ece3500d7', '创建用户组', 'create role', 'role');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('9916879f-457d-4d66-bc82-5c584d601023', '编辑他人的题目', 'edit others question', 'question');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('a9c06666-b0c6-4da3-b931-1e88f6e68568', '更改用户所属组', 'change role', 'role');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('ac3ae336-494d-99fb-599f-80119a039c0d', '修改其他设置', 'save setting otherSetting', 'setting');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('b7d7ca43-7f6c-477f-a861-a14cb9e906dc', '修改用户所属组组到：user', 'change role user', 'role');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('cd5ca696-b5ec-46cc-8f31-2752f82ab811', '修改用户所属组组到：advanced user', 'change role advanced user', 'role');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('dc64a9e4-2f97-425b-9e8d-6c32c155094f', '创建和修改自己的题目', 'create and edit owns question', 'question');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('e3398957-89a8-4ac2-960a-0a80599d5da2', '删除他人的题目', 'delete others question', 'question');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('e49f7e9d-9ff9-4e86-b4a6-d08ecdb353eb', '删除自己的题目', 'delete owns question', 'question');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('f7cebeB8-df52-41bd-8717-1f469cbe2a4b', '修改校验设置', 'save setting verificationSetting', 'setting');
INSERT INTO check_in.permissions (id, description, name, group_name) VALUES ('fe15070c-3466-4be9-a529-b15918ad34f8', '修改题目作者', 'change author', 'question');
