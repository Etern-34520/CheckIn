create table server_setting_items
(
    setting_key   varchar(255) not null
        primary key,
    setting_value varchar(255) null
);

INSERT INTO checkin.server_setting_items (setting_key, setting_value) VALUES ('checking.enableAutoCreateUser', 'true');
INSERT INTO checkin.server_setting_items (setting_key, setting_value) VALUES ('checking.notPassMessage', 'message: not passed');
INSERT INTO checkin.server_setting_items (setting_key, setting_value) VALUES ('checking.passMessage', 'message: passed');
INSERT INTO checkin.server_setting_items (setting_key, setting_value) VALUES ('checking.passScore', '60');
INSERT INTO checkin.server_setting_items (setting_key, setting_value) VALUES ('exam.description', 'test');
INSERT INTO checkin.server_setting_items (setting_key, setting_value) VALUES ('exam.partitionCountMax', '1');
INSERT INTO checkin.server_setting_items (setting_key, setting_value) VALUES ('exam.partitionCountMin', '1');
INSERT INTO checkin.server_setting_items (setting_key, setting_value) VALUES ('exam.questionCount', '0');
INSERT INTO checkin.server_setting_items (setting_key, setting_value) VALUES ('exam.title', 'HELLO');
INSERT INTO checkin.server_setting_items (setting_key, setting_value) VALUES ('other.defaultPartitionName', 'undefined');
INSERT INTO checkin.server_setting_items (setting_key, setting_value) VALUES ('other.robotToken', '46246323-D671-0BEB-1615-5936CE68721A');