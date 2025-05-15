/* Rename role permission*/
UPDATE permissions
SET description = replace(description, '修改用户所属组组到 ', '操作用户组 '),
    name = replace(name, 'change role to ', 'operate role ')
WHERE group_name = 'role';