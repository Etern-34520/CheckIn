INSERT INTO permission_groups (name, description)
VALUES ('manage user', '管理用户'),
       ('partition', '分区'),
       ('question', '题目'),
       ('question group', '题组'),
       ('role', '管理用户组'),
       ('setting', '修改设置'),
       ('exam data', '试题记录'),
       ('request record', '请求记录');

INSERT INTO permissions (id, description, name, group_name)
VALUES ('02c72178-bac2-424c-aa38-1c60a2aa1d6e', '添加用户', 'create user', 'manage user'),
       ('186c3767-02af-48b8-84a1-24cdabc19961', '启/禁用用户', 'change user state', 'manage user'),
       ('8f8e65b1-968b-4e47-a26f-466ab6e095b8', '删除用户', 'delete user', 'manage user'),
       ('cf31e280-1f42-45eb-bd46-a93782168328', '修改用户名称', 'change user name', 'manage user'),

       ('2c9e517f-6edf-4cc9-a5e6-a582a87c3753', '删除分区', 'delete partition', 'partition'),
       ('2e23a0dc-ecc2-448e-92d5-09d698553d31', '创建分区', 'create partition', 'partition'),
       ('4be6129d-b3b0-41df-9fcc-946ab36545dc', '编辑分区名称', 'edit partition name', 'partition'),

       ('9916879f-457d-4d66-bc82-5c584d601023', '编辑他人的题目', 'edit others questions', 'question'),
       ('4d0be824-2e7b-48fa-aba3-a1a37c452e23', '启/禁用题目', 'enable and disable questions', 'question'),
       ('dc64a9e4-2f97-425b-9e8d-6c32c155094f', '创建和修改自己的题目', 'create and edit owns questions', 'question'),
       ('e3398957-89a8-4ac2-960a-0a80599d5da2', '删除他人的题目', 'delete others question', 'question'),
       ('e49f7e9d-9ff9-4e86-b4a6-d08ecdb353eb', '删除自己的题目', 'delete owns questions', 'question'),
       ('fe15070c-3466-4be9-a529-b15918ad34f8', '修改题目作者', 'change question author', 'question'),

       ('95d02dee-6597-470a-b362-134ff7e97836', '编辑他人的题组', 'edit others question groups', 'question group'),
       ('2d9f4b13-aa1a-4dbc-862b-f8908f4573b0', '启/禁用题组', 'enable and disable question groups', 'question group'),
       ('d7bb3cc3-9a28-4302-9d20-2ac55d5188f5', '创建和修改自己的题组', 'create and edit owns question groups','question group'),
       ('8a5955c5-1efa-45a8-b81e-ab48c198f140', '删除他人的题组', 'delete others question groups', 'question group'),
       ('e0f7e540-3a7c-499b-952d-a4785778c6be', '删除自己的题组', 'delete owns question groups', 'question group'),
       ('e21f3c78-6139-45b7-b1c5-1245040c1c33', '修改题组作者', 'change question group author', 'question group'),

       ('6aa6faef-510b-41b6-a4bc-6ee51a8d19a6', '删除用户组', 'delete role', 'role'),
       ('9809102d-91a5-4b6e-8a31-1f2ece3500d7', '创建用户组', 'create role', 'role'),
       ('58bed2fa-ca97-411c-b511-e7bf31a435cc', '修改用户组权限', 'edit permission', 'role'),
       ('1692eeda-c00a-4bb2-b176-e109e462d11e', '修改用户组排列等级', 'update role level', 'role'),
       ('07a13959-7044-4f7c-bf4c-661853871700', '修改用户所属组组到 admin', 'change role to admin', 'role'),
       ('67b26d13-8373-4264-ba2a-4d8201ceebb0', '修改用户所属组组到 super admin', 'change role to super admin', 'role'),
       ('b7d7ca43-7f6c-477f-a861-a14cb9e906dc', '修改用户所属组组到 user', 'change role to user', 'role'),
       ('cd5ca696-b5ec-46cc-8f31-2752f82ab811', '修改用户所属组组到 advanced user', 'change role to advanced user',
        'role'),

       ('9bdc8382-ae40-4320-bad3-73ddb5206848', '获取他人试题记录', 'get exam data', 'exam data'),
       ('0eccd16d-5dce-4df8-9f8c-45a7d2df2f31', '获取他人试题提交内容', 'get exam submission data', 'exam data'),
       ('bbe16951-575d-478c-87d7-06d7dd1c5567', '手动无效化试题', 'manual invalid exam', 'exam data'),

       ('650e15dc-39f5-40d2-b64e-23b7725851d6', '获取请求记录', 'get request records', 'request record'),

       ('ac3ae336-494d-99fb-599f-80119a039c0d', '修改高级设置', 'save advance setting', 'setting'),
       ('18a698c5-199a-4ee6-be94-3a9b2b712c59', '修改首页设置', 'save facade setting', 'setting'),
       ('837B1e7d-4f84-4a21-b921-87eb6936f716', '修改生成设置', 'save generating setting', 'setting'),
       ('9e3bc5cb-2b52-40d1-9131-29b79dd86a78', '修改评分设置', 'save grading setting', 'setting'),
       ('f7cebeB8-df52-41bd-8717-1f469cbe2a4b', '修改校验设置', 'save verification setting', 'setting'),
       ('e7bb795c-5e37-4b32-8cd6-cdf43b55c68a', '获取高级设置', 'get advance setting', 'setting'),
       ('82b8b209-f265-4d5b-8d4d-cf9dd81657b8', '获取生成设置', 'get generating setting', 'setting');

INSERT INTO role (type, level)
VALUES ('admin', 1),
       ('advanced user', 2),
       ('super admin', 0),
       ('user', 3);

INSERT INTO role_permission_mapping (role_type, permission_id)
VALUES ('admin', '02c72178-bac2-424c-aa38-1c60a2aa1d6e'),
       ('admin', '186c3767-02af-48b8-84a1-24cdabc19961'),
       ('admin', '18a698c5-199a-4ee6-be94-3a9b2b712c59'),
       ('admin', '2b37d57f-fdb6-4bab-9cea-04488aac87f9'),
       ('admin', '2c9e517f-6edf-4cc9-a5e6-a582a87c3753'),
       ('admin', '2d9f4b13-aa1a-4dbc-862b-f8908f4573b0'),
       ('admin', '2e23a0dc-ecc2-448e-92d5-09d698553d31'),
       ('admin', '4be6129d-b3b0-41df-9fcc-946ab36545dc'),
       ('admin', '4d0be824-2e7b-48fa-aba3-a1a37c452e23'),
       ('admin', '650e15dc-39f5-40d2-b64e-23b7725851d6'),
       ('admin', '82b8b209-f265-4d5b-8d4d-cf9dd81657b8'),
       ('admin', '837B1e7d-4f84-4a21-b921-87eb6936f716'),
       ('admin', '8a5955c5-1efa-45a8-b81e-ab48c198f140'),
       ('admin', '8f8e65b1-968b-4e47-a26f-466ab6e095b8'),
       ('admin', '95d02dee-6597-470a-b362-134ff7e97836'),
       ('admin', '9916879f-457d-4d66-bc82-5c584d601023'),
       ('admin', '9bdc8382-ae40-4320-bad3-73ddb5206848'),
       ('admin', '9e3bc5cb-2b52-40d1-9131-29b79dd86a78'),
       ('admin', 'ac3ae336-494d-99fb-599f-80119a039c0d'),
       ('admin', 'b7d7ca43-7f6c-477f-a861-a14cb9e906dc'),
       ('admin', 'bbe16951-575d-478c-87d7-06d7dd1c5567'),
       ('admin', 'cd5ca696-b5ec-46cc-8f31-2752f82ab811'),
       ('admin', 'cf31e280-1f42-45eb-bd46-a93782168328'),
       ('admin', 'd7bb3cc3-9a28-4302-9d20-2ac55d5188f5'),
       ('admin', 'dc64a9e4-2f97-425b-9e8d-6c32c155094f'),
       ('admin', 'e0f7e540-3a7c-499b-952d-a4785778c6be'),
       ('admin', 'e21f3c78-6139-45b7-b1c5-1245040c1c33'),
       ('admin', 'e3398957-89a8-4ac2-960a-0a80599d5da2'),
       ('admin', 'e49f7e9d-9ff9-4e86-b4a6-d08ecdb353eb'),
       ('admin', 'e7bb795c-5e37-4b32-8cd6-cdf43b55c68a'),
       ('admin', 'f7cebeB8-df52-41bd-8717-1f469cbe2a4b'),
       ('admin', 'fe15070c-3466-4be9-a529-b15918ad34f8'),
       ('admin', '0eccd16d-5dce-4df8-9f8c-45a7d2df2f31'),

       ('advanced user', '2b37d57f-fdb6-4bab-9cea-04488aac87f9'),
       ('advanced user', '2c9e517f-6edf-4cc9-a5e6-a582a87c3753'),
       ('advanced user', '2d9f4b13-aa1a-4dbc-862b-f8908f4573b0'),
       ('advanced user', '2e23a0dc-ecc2-448e-92d5-09d698553d31'),
       ('advanced user', '4be6129d-b3b0-41df-9fcc-946ab36545dc'),
       ('advanced user', '4d0be824-2e7b-48fa-aba3-a1a37c452e23'),
       ('advanced user', '650e15dc-39f5-40d2-b64e-23b7725851d6'),
       ('advanced user', '82b8b209-f265-4d5b-8d4d-cf9dd81657b8'),
       ('advanced user', '8a5955c5-1efa-45a8-b81e-ab48c198f140'),
       ('advanced user', '95d02dee-6597-470a-b362-134ff7e97836'),
       ('advanced user', '9916879f-457d-4d66-bc82-5c584d601023'),
       ('advanced user', '9bdc8382-ae40-4320-bad3-73ddb5206848'),
       ('advanced user', 'bbe16951-575d-478c-87d7-06d7dd1c5567'),
       ('advanced user', 'd7bb3cc3-9a28-4302-9d20-2ac55d5188f5'),
       ('advanced user', 'dc64a9e4-2f97-425b-9e8d-6c32c155094f'),
       ('advanced user', 'e0f7e540-3a7c-499b-952d-a4785778c6be'),
       ('advanced user', 'e21f3c78-6139-45b7-b1c5-1245040c1c33'),
       ('advanced user', 'e3398957-89a8-4ac2-960a-0a80599d5da2'),
       ('advanced user', 'e49f7e9d-9ff9-4e86-b4a6-d08ecdb353eb'),
       ('advanced user', 'e7bb795c-5e37-4b32-8cd6-cdf43b55c68a'),
       ('advanced user', 'fe15070c-3466-4be9-a529-b15918ad34f8'),

       ('super admin', '02c72178-bac2-424c-aa38-1c60a2aa1d6e'),
       ('super admin', '07a13959-7044-4f7c-bf4c-661853871700'),
       ('super admin', '1692eeda-c00a-4bb2-b176-e109e462d11e'),
       ('super admin', '186c3767-02af-48b8-84a1-24cdabc19961'),
       ('super admin', '18a698c5-199a-4ee6-be94-3a9b2b712c59'),
       ('super admin', '2b37d57f-fdb6-4bab-9cea-04488aac87f9'),
       ('super admin', '2c9e517f-6edf-4cc9-a5e6-a582a87c3753'),
       ('super admin', '2d9f4b13-aa1a-4dbc-862b-f8908f4573b0'),
       ('super admin', '2e23a0dc-ecc2-448e-92d5-09d698553d31'),
       ('super admin', '4be6129d-b3b0-41df-9fcc-946ab36545dc'),
       ('super admin', '4d0be824-2e7b-48fa-aba3-a1a37c452e23'),
       ('super admin', '58bed2fa-ca97-411c-b511-e7bf31a435cc'),
       ('super admin', '650e15dc-39f5-40d2-b64e-23b7725851d6'),
       ('super admin', '67b26d13-8373-4264-ba2a-4d8201ceebb0'),
       ('super admin', '6aa6faef-510b-41b6-a4bc-6ee51a8d19a6'),
       ('super admin', '82b8b209-f265-4d5b-8d4d-cf9dd81657b8'),
       ('super admin', '837B1e7d-4f84-4a21-b921-87eb6936f716'),
       ('super admin', '8a5955c5-1efa-45a8-b81e-ab48c198f140'),
       ('super admin', '8f8e65b1-968b-4e47-a26f-466ab6e095b8'),
       ('super admin', '95d02dee-6597-470a-b362-134ff7e97836'),
       ('super admin', '9809102d-91a5-4b6e-8a31-1f2ece3500d7'),
       ('super admin', '9916879f-457d-4d66-bc82-5c584d601023'),
       ('super admin', '9bdc8382-ae40-4320-bad3-73ddb5206848'),
       ('super admin', '9e3bc5cb-2b52-40d1-9131-29b79dd86a78'),
       ('super admin', 'ac3ae336-494d-99fb-599f-80119a039c0d'),
       ('super admin', 'b7d7ca43-7f6c-477f-a861-a14cb9e906dc'),
       ('super admin', 'bbe16951-575d-478c-87d7-06d7dd1c5567'),
       ('super admin', 'cd5ca696-b5ec-46cc-8f31-2752f82ab811'),
       ('super admin', 'cf31e280-1f42-45eb-bd46-a93782168328'),
       ('super admin', 'd7bb3cc3-9a28-4302-9d20-2ac55d5188f5'),
       ('super admin', 'dc64a9e4-2f97-425b-9e8d-6c32c155094f'),
       ('super admin', 'e0f7e540-3a7c-499b-952d-a4785778c6be'),
       ('super admin', 'e21f3c78-6139-45b7-b1c5-1245040c1c33'),
       ('super admin', 'e3398957-89a8-4ac2-960a-0a80599d5da2'),
       ('super admin', 'e49f7e9d-9ff9-4e86-b4a6-d08ecdb353eb'),
       ('super admin', 'e7bb795c-5e37-4b32-8cd6-cdf43b55c68a'),
       ('super admin', 'f7cebeB8-df52-41bd-8717-1f469cbe2a4b'),
       ('super admin', 'fe15070c-3466-4be9-a529-b15918ad34f8'),
       ('super admin', '0eccd16d-5dce-4df8-9f8c-45a7d2df2f31'),

       ('user', '2d9f4b13-aa1a-4dbc-862b-f8908f4573b0'),
       ('user', '4d0be824-2e7b-48fa-aba3-a1a37c452e23'),
       ('user', 'd7bb3cc3-9a28-4302-9d20-2ac55d5188f5'),
       ('user', 'dc64a9e4-2f97-425b-9e8d-6c32c155094f'),
       ('user', 'e0f7e540-3a7c-499b-952d-a4785778c6be'),
       ('user', 'e49f7e9d-9ff9-4e86-b4a6-d08ecdb353eb');

INSERT INTO server_setting_items (setting_key, setting_value, clazz)
VALUES ('generating.completingPartitions', '[]', 'java.util.ArrayList'),
       ('generating.completingStrategy', '"required"', 'java.lang.String'),
       ('generating.drawingStrategy', '"weighted"', 'java.lang.String'),
       ('generating.partitionRange', '[0,1]', 'java.util.ArrayList'),
       ('generating.questionAmount', '0', 'java.lang.Integer'),
       ('generating.questionScore', '5', 'java.lang.Integer'),
       ('generating.requiredPartitions', '[]', 'java.util.ArrayList'),
       ('generating.showRequiredPartitions', 'true', 'java.lang.Boolean'),
       ('generating.specialPartitionLimits', '{}', 'java.util.LinkedHashMap'),
       ('facade.description', '""', 'java.lang.String'),
       ('facade.icon', '"data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgdmlld0JveD0iMCAwIDMyMCAzMjAiPg0KPHRpdGxlPkNoZWNrSW48L3RpdGxlPg0KICAgIDxnPg0KICAgICAgICA8dGl0bGU+TGF5ZXIgMTwvdGl0bGU+DQogICAgICAgIDxwYXRoIGQ9Im0xNjAsMjUwYTkwLDkwIDAgMSAxIDYzLjYzOTYxLC0xNTMuNjM5NjFtNi4zNjAzOSw2My42Mzk2MWwwLDkwIiBzdHJva2U9IiM4ZmExYjMiIGZpbGw9InRyYW5zcGFyZW50IiBzdHJva2Utd2lkdGg9IjQwIiBzdHJva2UtbGluZWNhcD0icm91bmQiIGlkPSJzdmdfMSIvPg0KICAgIDwvZz4NCjwvc3ZnPg==""', 'java.lang.String'),
       ('facade.subTitle', '"SubTitle"', 'java.lang.String'),
       ('facade.title', '"Title"', 'java.lang.String'),
       ('grading.splits', '[0]', 'java.util.ArrayList'),
       ('other.defaultPartitionName', '"未命名"', 'java.lang.String'),
       ('advance.autoCreateUserMode', '"disabled"', 'java.lang.String'),
       ('exam.expiredPeriod', '"P7D"', 'java.time.Period');

INSERT INTO users (qqnumber, enabled, name, password, role_type)
VALUES (10000, true, 'super admin', '$2a$10$Ve6WIqVRVN5zPXzMssxE0.cyD6XuTa2PMwOss9q7QC/RrAtWXit1C', 'super admin');