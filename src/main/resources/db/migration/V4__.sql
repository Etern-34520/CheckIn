ALTER TABLE verification_rules
    ADD ignore_missing_field TINYINT(1) DEFAULT 0 NULL;
UPDATE verification_rules
SET ignore_missing_field = 1
WHERE target_input_name = 'partitions';

UPDATE questions SET verification_digest = NULL, validation_warnings = NULL , validation_errors = NULL;

ALTER TABLE questions
    ADD show_validation_errors TINYINT(1) DEFAULT 0 NULL;

ALTER TABLE questions
    ADD show_validation_warnings TINYINT(1) DEFAULT 0 NULL;