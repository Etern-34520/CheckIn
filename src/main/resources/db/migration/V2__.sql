# Add auto creating user support
ALTER TABLE grading_levels
    ADD creating_user_strategy TINYINT NOT NULL,
    ADD level_index INT NOT NULL;

# Rename role permission
UPDATE permissions
SET description = replace(description, '修改用户所属组组到 ', '操作用户组 '),
    name = replace(name, 'change role to ', 'operate role ')
WHERE group_name = 'role';