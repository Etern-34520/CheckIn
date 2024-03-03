create table server_setting_items (
    setting_key   varchar(255) not null
        primary key,
    setting_value varchar(255) null
);

INSERT INTO checkIn.server_setting_items (setting_key, setting_value) VALUES ('checking.enableAutoCreateUser', 'true');
INSERT INTO checkIn.server_setting_items (setting_key, setting_value) VALUES ('checking.failMessage', '2');
INSERT INTO checkIn.server_setting_items (setting_key, setting_value) VALUES ('checking.notPassMessage', 'message: not passed');
INSERT INTO checkIn.server_setting_items (setting_key, setting_value) VALUES ('checking.passMessage', 'message: passed');
INSERT INTO checkIn.server_setting_items (setting_key, setting_value) VALUES ('checking.passScore', '60');
INSERT INTO checkIn.server_setting_items (setting_key, setting_value) VALUES ('exam.description', '让我猜猜，你是一位资深的电教委
或者
每天来到班里，看到的全是电脑砖家
找不到志同道合的网友交流见解
欢迎加入  <a href="https://harkerbests-organization.gitbook.io/xi-wo-shou-hou-ye-ji-chong-ji-bu/">希沃售后业绩冲击部</a>');
INSERT INTO checkIn.server_setting_items (setting_key, setting_value) VALUES ('exam.partitionCountMax', '1');
INSERT INTO checkIn.server_setting_items (setting_key, setting_value) VALUES ('exam.partitionCountMin', '1');
INSERT INTO checkIn.server_setting_items (setting_key, setting_value) VALUES ('exam.questionCount', '1');
INSERT INTO checkIn.server_setting_items (setting_key, setting_value) VALUES ('exam.title', 'HELLO');
INSERT INTO checkIn.server_setting_items (setting_key, setting_value) VALUES ('examDescription', '');
INSERT INTO checkIn.server_setting_items (setting_key, setting_value) VALUES ('examQuestionCount', '');
INSERT INTO checkIn.server_setting_items (setting_key, setting_value) VALUES ('examTitle', '');
INSERT INTO checkIn.server_setting_items (setting_key, setting_value) VALUES ('max_value', '1');
INSERT INTO checkIn.server_setting_items (setting_key, setting_value) VALUES ('min_value', '1');
