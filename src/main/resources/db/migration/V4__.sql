ALTER TABLE verification_rules
    ADD ignore_missing_field BOOLEAN DEFAULT FALSE NULL;
UPDATE verification_rules
SET ignore_missing_field = 1
WHERE target_input_name = 'partitions';

UPDATE questions SET verification_digest = NULL, validation_warnings = NULL , validation_errors = NULL;

ALTER TABLE questions
    ADD show_validation_errors BOOLEAN DEFAULT FALSE NULL;

ALTER TABLE questions
    ADD show_validation_warnings BOOLEAN DEFAULT FALSE NULL;