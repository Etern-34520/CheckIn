ALTER TABLE questions
    ADD validation_errors VARCHAR(4096) NULL;

ALTER TABLE questions
    ADD validation_warnings VARCHAR(4096) NULL;

ALTER TABLE questions
    ADD verification_digest CHAR(32) NULL;