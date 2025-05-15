CREATE TABLE downvoters_questions_mapping
(
    question_id CHAR(36) NOT NULL,
    user_id     BIGINT   NOT NULL,
    CONSTRAINT pk_downvoters_questions_mapping PRIMARY KEY (question_id, user_id)
);

CREATE TABLE exam_data
(
    id              CHAR(36) NOT NULL PRIMARY KEY,
    answers         LONGTEXT NULL,
    exam_result     LONGTEXT NULL,
    expire_time     datetime NULL,
    generate_time   datetime NULL,
    qq_number       BIGINT   NOT NULL,
    question_amount INT      NOT NULL,
    status          TINYINT  NULL,
    submit_time     datetime NULL
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

CREATE TABLE grading_levels
(
    id                     CHAR(36)     NOT NULL PRIMARY KEY,
    color_hex              CHAR(7)      NULL,
    `description`          VARCHAR(255) NULL,
    message                VARCHAR(255) NULL,
    name                   VARCHAR(255) NULL,
    creating_user_strategy TINYINT      NOT NULL,
    level_index            INT          NOT NULL,
    role_type              VARCHAR(255) NULL
);

CREATE TABLE multiple_choices_question_choices
(
    multiple_choices_question_id CHAR(36)     NOT NULL,
    content                      VARCHAR(255) NULL,
    id                           CHAR(36)     NOT NULL,
    is_correct                   BOOLEAN      NULL,
    order_index                  INT          NULL,
    CONSTRAINT id_unique_constraint UNIQUE (id)
);

CREATE TABLE partitions
(
    id   CHAR(36)     NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE permission_groups
(
    name          VARCHAR(255) NOT NULL PRIMARY KEY,
    `description` VARCHAR(255) NULL
);

CREATE TABLE permissions
(
    id            CHAR(36)     NOT NULL PRIMARY KEY,
    `description` VARCHAR(255) NULL,
    name          VARCHAR(255) NOT NULL,
    group_name    VARCHAR(255) NULL
);

CREATE TABLE question_link
(
    dtype       VARCHAR(31) NOT NULL,
    id          CHAR(36)    NOT NULL PRIMARY KEY,
    link_type   TINYINT     NULL,
    order_index INT         NULL,
    target_id   CHAR(36)    NULL
);

CREATE TABLE question_statistics
(
    id              CHAR(36) NOT NULL PRIMARY KEY,
    correct_count   INT      NOT NULL,
    drew_count      INT      NOT NULL,
    submitted_count INT      NOT NULL,
    wrong_count     INT      NOT NULL
);

CREATE TABLE questions
(
    dtype                VARCHAR(31) NOT NULL,
    id                   CHAR(36)    NOT NULL PRIMARY KEY,
    content              LONGTEXT    NULL,
    enabled              BOOLEAN     NOT NULL,
    image_base64_strings MEDIUMBLOB        NULL,
    last_modified_time   datetime    NULL,
    random_ordered       BOOLEAN     NULL,
    sub_type             TINYINT     NULL,
    author_qqnumber      BIGINT      NULL
);

CREATE TABLE questions_link_mapping
(
    question_id  CHAR(36) NOT NULL,
    partition_id CHAR(36) NOT NULL,
    CONSTRAINT pk_questions_link_mapping PRIMARY KEY (question_id, partition_id)
);

CREATE TABLE records
(
    id                   CHAR(36)     NOT NULL PRIMARY KEY,
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
    type                 TINYINT      NULL
);

CREATE TABLE robot_tokens
(
    id                 CHAR(36)      NOT NULL PRIMARY KEY,
    `description`      VARCHAR(1024) NULL,
    generate_by_userqq BIGINT        NULL,
    generate_time      datetime      NULL,
    token              VARCHAR(512)  NULL
);

CREATE TABLE `role`
(
    type  VARCHAR(255) NOT NULL PRIMARY KEY,
    level INT          NOT NULL
);

CREATE TABLE role_permission_mapping
(
    role_type     VARCHAR(255) NOT NULL,
    permission_id CHAR(36)     NOT NULL,
    CONSTRAINT pk_role_permission_mapping PRIMARY KEY (role_type, permission_id)
);

CREATE TABLE server_setting_items
(
    setting_key   VARCHAR(255) NOT NULL PRIMARY KEY,
    clazz         VARCHAR(255) NULL,
    setting_value LONGTEXT     NULL
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
    CONSTRAINT pk_upvoters_questions_mapping PRIMARY KEY (question_id, user_id)
);

CREATE TABLE users
(
    qqnumber  BIGINT       NOT NULL PRIMARY KEY,
    enabled   BOOLEAN      NOT NULL,
    name      VARCHAR(255) NULL,
    password  VARCHAR(255) NULL,
    role_type VARCHAR(255) NULL
);

CREATE TABLE verification_rules
(
    id                CHAR(36)     NOT NULL PRIMARY KEY,
    order_index       INT          NULL,
    level             VARCHAR(255) NULL,
    object_name       VARCHAR(255) NULL,
    target_input_name VARCHAR(255) NULL,
    tip_template      VARCHAR(255) NULL,
    trace             VARCHAR(255) NULL,
    data_values       VARCHAR(255) NULL,
    verification_type VARCHAR(255) NULL
);

/*ALTER TABLE multiple_choices_question_choices
    ADD CONSTRAINT id_unique_constraint UNIQUE (id);*/

ALTER TABLE partitions
    ADD CONSTRAINT name_unique_constraint UNIQUE (name);