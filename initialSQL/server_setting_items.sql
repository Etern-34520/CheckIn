create table server_setting_items
(
    setting_key   varchar(255) not null
        primary key,
    setting_value mediumtext   null,
    clazz         text         null
);

INSERT INTO check_in.server_setting_items (setting_key, setting_value, clazz) VALUES ('drawing.completingPartitions', '[]', 'java.util.ArrayList');
INSERT INTO check_in.server_setting_items (setting_key, setting_value, clazz) VALUES ('drawing.completingStrategy', '"selected"', 'java.lang.String');
INSERT INTO check_in.server_setting_items (setting_key, setting_value, clazz) VALUES ('drawing.drawingStrategy', '"weighted"', 'java.lang.String');
INSERT INTO check_in.server_setting_items (setting_key, setting_value, clazz) VALUES ('drawing.partitionRange', '[]', 'java.util.ArrayList');
INSERT INTO check_in.server_setting_items (setting_key, setting_value, clazz) VALUES ('drawing.questionAmount', '0', 'java.lang.Integer');
INSERT INTO check_in.server_setting_items (setting_key, setting_value, clazz) VALUES ('drawing.requiredPartitions', '[]', 'java.util.ArrayList');
INSERT INTO check_in.server_setting_items (setting_key, setting_value, clazz) VALUES ('drawing.specialPartitionLimits', '{}', 'java.util.LinkedHashMap');
INSERT INTO check_in.server_setting_items (setting_key, setting_value, clazz) VALUES ('facade.description', '""', 'java.lang.String');
INSERT INTO check_in.server_setting_items (setting_key, setting_value, clazz) VALUES ('facade.icon', '""', 'java.lang.String');
INSERT INTO check_in.server_setting_items (setting_key, setting_value, clazz) VALUES ('facade.subTitle', '"check in"', 'java.lang.String');
INSERT INTO check_in.server_setting_items (setting_key, setting_value, clazz) VALUES ('facade.title', '"CHECK IN"', 'java.lang.String');
