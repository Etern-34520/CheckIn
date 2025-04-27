CREATE TABLE downvoters_questions_mapping
(
    question_id CHAR(36) NOT NULL,
    user_id     BIGINT   NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (question_id, user_id)
);

CREATE TABLE exam_data
(
    id              CHAR(36) NOT NULL,
    answers         LONGTEXT NULL,
    exam_result     LONGTEXT NULL,
    expire_time     datetime NULL,
    generate_time   datetime NULL,
    qq_number       BIGINT   NOT NULL,
    question_amount INT      NOT NULL,
    status          TINYINT  NULL,
    submit_time     datetime NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE exam_data_question_ids
(
    exam_data_id CHAR(36)     NOT NULL,
    question_ids VARCHAR(255) NULL
);

CREATE TABLE exam_data_required_partition_ids
(
    exam_data_id           CHAR(36)     NOT NULL,
    required_partition_ids VARCHAR(255) NULL
);

CREATE TABLE exam_data_selected_partition_ids
(
    exam_data_id           CHAR(36)     NOT NULL,
    selected_partition_ids VARCHAR(255) NULL
);

/*CREATE TABLE flyway_schema_history
(
    installed_rank INT                     NOT NULL,
    version        VARCHAR(50)             NULL,
    `description`  VARCHAR(200)            NOT NULL,
    type           VARCHAR(20)             NOT NULL,
    script         VARCHAR(1000)           NOT NULL,
    checksum       INT                     NULL,
    installed_by   VARCHAR(100)            NOT NULL,
    installed_on   timestamp DEFAULT NOW() NOT NULL,
    execution_time INT                     NOT NULL,
    success        TINYINT(1)              NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (installed_rank)
);*/

CREATE TABLE grading_levels
(
    id            CHAR(36)     NOT NULL,
    color_hex     CHAR(7)      NULL,
    `description` VARCHAR(255) NULL,
    message       VARCHAR(255) NULL,
    name          VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE multiple_choices_question_choices
(
    multiple_choices_question_id CHAR(36)     NOT NULL,
    content                      VARCHAR(255) NULL,
    id                           CHAR(36)     NOT NULL,
    is_correct                   BIT(1)       NULL,
    order_index                  INT          NULL
);

CREATE TABLE partitions
(
    id   CHAR(36)     NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE permission_groups
(
    name          VARCHAR(255) NOT NULL,
    `description` VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (name)
);

CREATE TABLE permissions
(
    id            CHAR(36)     NOT NULL,
    `description` VARCHAR(255) NULL,
    name          VARCHAR(255) NOT NULL,
    group_name    VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE question_link
(
    dtype       VARCHAR(31) NOT NULL,
    id          CHAR(36)    NOT NULL,
    link_type   TINYINT     NULL,
    order_index INT         NULL,
    target_id   CHAR(36)    NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE question_statistics
(
    id              CHAR(36) NOT NULL,
    correct_count   INT      NOT NULL,
    drew_count      INT      NOT NULL,
    submitted_count INT      NOT NULL,
    wrong_count     INT      NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE questions
(
    dtype                VARCHAR(31) NOT NULL,
    id                   CHAR(36)    NOT NULL,
    content              LONGTEXT    NULL,
    enabled              BIT(1)      NOT NULL,
    image_base64_strings BLOB        NULL,
    last_modified_time   datetime    NULL,
    random_ordered       BIT(1)      NULL,
    sub_type             TINYINT     NULL,
    author_qqnumber      BIGINT      NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE questions_link_mapping
(
    question_id  CHAR(36) NOT NULL,
    partition_id CHAR(36) NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (question_id, partition_id)
);

CREATE TABLE records
(
    id                   CHAR(36)     NOT NULL,
    qq                   BIGINT       NULL,
    extra_data           LONGTEXT     NULL,
    ip_string            VARCHAR(255) NULL,
    related_exam_data_id CHAR(36)     NULL,
    request_attributes   LONGTEXT     NULL,
    request_headers      LONGTEXT     NULL,
    response_headers     LONGTEXT     NULL,
    session_id           CHAR(32)     NULL,
    status               TINYINT      NULL,
    record_time          datetime     NULL,
    type                 TINYINT      NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE robot_tokens
(
    id                 CHAR(36)      NOT NULL,
    `description`      VARCHAR(1024) NULL,
    generate_by_userqq BIGINT        NULL,
    generate_time      datetime      NULL,
    token              VARCHAR(512)  NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE `role`
(
    type  VARCHAR(255) NOT NULL,
    level INT          NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (type)
);

CREATE TABLE role_permission_mapping
(
    role_type     VARCHAR(255) NOT NULL,
    permission_id CHAR(36)     NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (role_type, permission_id)
);

CREATE TABLE server_setting_items
(
    setting_key   VARCHAR(255) NOT NULL,
    clazz         VARCHAR(255) NULL,
    setting_value LONGTEXT     NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (setting_key)
);

CREATE TABLE statistic_exam_data_mapping
(
    exam_id      CHAR(36) NOT NULL,
    statistic_id CHAR(36) NOT NULL
);

CREATE TABLE upvoters_questions_mapping
(
    question_id CHAR(36) NOT NULL,
    user_id     BIGINT   NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (question_id, user_id)
);

CREATE TABLE users
(
    qqnumber  BIGINT       NOT NULL,
    enabled   BIT(1)       NOT NULL,
    name      VARCHAR(255) NULL,
    password  VARCHAR(255) NULL,
    role_type VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (qqnumber)
);

CREATE TABLE verification_rules
(
    id                CHAR(36)     NOT NULL,
    order_index       INT          NULL,
    level             VARCHAR(255) NULL,
    object_name       VARCHAR(255) NULL,
    target_input_name VARCHAR(255) NULL,
    tip_template      VARCHAR(255) NULL,
    trace             VARCHAR(255) NULL,
    data_values       VARCHAR(255) NULL,
    verification_type VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

ALTER TABLE multiple_choices_question_choices
    ADD CONSTRAINT id_unique_constraint UNIQUE (id);

ALTER TABLE partitions
    ADD CONSTRAINT name_unique_constraint UNIQUE (name);

# CREATE INDEX flyway_schema_history_s_idx ON flyway_schema_history (success);

# ALTER TABLE questions_link_mapping
#     ADD CONSTRAINT FK165owwttcamvegj7x0p284wkf FOREIGN KEY (partition_id) REFERENCES partitions (id) ON DELETE NO ACTION;

# CREATE INDEX FK165owwttcamvegj7x0p284wkf ON questions_link_mapping (partition_id);

# ALTER TABLE multiple_choices_question_choices
#     ADD CONSTRAINT FKg2479mrt2fbxdbli7jl21qn84 FOREIGN KEY (multiple_choices_question_id) REFERENCES questions (id) ON DELETE NO ACTION;

# CREATE INDEX FKg2479mrt2fbxdbli7jl21qn84 ON multiple_choices_question_choices (multiple_choices_question_id);

# ALTER TABLE questions_link_mapping
#     ADD CONSTRAINT FKh8sank8c649elw02e5p49b8mn FOREIGN KEY (question_id) REFERENCES question_link (id) ON DELETE NO ACTION;