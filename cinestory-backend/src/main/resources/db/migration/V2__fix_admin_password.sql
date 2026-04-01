-- 修复管理员默认密码为 admin123
UPDATE users SET password = '$2a$10$jn62Tw6DDlvKZCChZW7Wou.GHZj0eMGRppH2wDa/AQUAj6wzNF20W'
WHERE username = 'admin';
