ALTER TABLE grading_levels MODIFY message TEXT NULL;
ALTER TABLE grading_levels MODIFY description TEXT NULL;

ALTER TABLE questions ADD unsafe_xss tinyint(1) NOT NULL DEFAULT 0;
INSERT INTO permissions (id, description, name, group_name)
VALUES ('2a58bc78-a1cc-43d3-8481-808d2fe94004', '启/禁用题目 XSS', 'enable and disable unsafe xss for questions',
        'question');

INSERT INTO permissions (id, description, name, group_name)
VALUES ('88c79d17-0a49-4ec6-8355-f302ad0c04a2', '启/禁用题组 XSS', 'enable and disable unsafe xss for question groups',
        'question group');

INSERT INTO role_permission_mapping (role_type, permission_id)
VALUES ('super admin', '2a58bc78-a1cc-43d3-8481-808d2fe94004'),
       ('admin', '2a58bc78-a1cc-43d3-8481-808d2fe94004'),
       ('super admin', '88c79d17-0a49-4ec6-8355-f302ad0c04a2'),
       ('admin', '88c79d17-0a49-4ec6-8355-f302ad0c04a2');