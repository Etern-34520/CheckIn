CREATE TABLE oauth2_bindings
(
    id            CHAR(36)     NOT NULL,
    provider_id   VARCHAR(255) NULL,
    user_id       VARCHAR(255) NULL,
    user_qqnumber BIGINT       NULL,
    PRIMARY KEY (id)
);

CREATE TABLE oauth2_providers
(
    id                     VARCHAR(255) NOT NULL,
    order_index            INT          NOT NULL,
    name                   VARCHAR(255) NULL,
    icon_domain            VARCHAR(255) NULL,
    issuer_uri             VARCHAR(255) NULL,
    authorization_uri      VARCHAR(255) NULL,
    user_info_uri          VARCHAR(255) NULL,
    jwks_uri               VARCHAR(255) NULL,
    client_id              VARCHAR(255) NULL,
    client_secret          VARCHAR(255) NULL,
    scope                  VARCHAR(255) NULL,
    user_id_attribute_name VARCHAR(255) NULL,
    enabled                BOOLEAN      NULL,
    PRIMARY KEY (id)
);

CREATE TABLE oauth2_providers_info_attributes
(
    oauth2provider_info_id VARCHAR(255) NOT NULL,
    name                   VARCHAR(255) NULL,
    attr_value             VARCHAR(255) NULL,
    enabled                BOOLEAN      NULL
);

ALTER TABLE oauth2_bindings
    ADD CONSTRAINT UKfc1rn4atug8yp93j2newwvyl9 UNIQUE (provider_id, user_id);

CREATE INDEX FK165owwttcamvegj7x0p284wkf ON questions_link_mapping (partition_id);
CREATE INDEX FKg2479mrt2fbxdbli7jl21qn84 ON multiple_choices_question_choices (multiple_choices_question_id);

CREATE INDEX FKqlu1gwwnhw0d74m1el5die6tp ON oauth2_bindings (user_qqnumber);

INSERT INTO permissions (id, description, name, group_name)
VALUES ('417d4bca-3180-498f-ae02-b9b9ecab6905', '获取 OAuth2 设置', 'get OAuth2 setting', 'setting'),
       ('520a7928-c4e8-4be9-9807-c190b329fb46', '修改 OAuth2 设置', 'save OAuth2 setting', 'setting');

INSERT INTO role_permission_mapping (role_type, permission_id)
VALUES ('super admin', '417d4bca-3180-498f-ae02-b9b9ecab6905'),
       ('admin', '417d4bca-3180-498f-ae02-b9b9ecab6905'),
       ('super admin', '520a7928-c4e8-4be9-9807-c190b329fb46'),
       ('admin', '520a7928-c4e8-4be9-9807-c190b329fb46');