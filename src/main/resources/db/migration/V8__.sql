ALTER TABLE oauth2_providers RENAME COLUMN enabled TO enabled_in_login;

ALTER TABLE oauth2_providers
    ADD exam_login_mode INT NULL;

ALTER TABLE exam_data
    ADD oauth2_bindings MEDIUMTEXT NULL;