INSERT INTO users (
    id,
    username,
    email,
    password_hash,
    enabled,
    account_non_expired,
    account_non_locked,
    credentials_non_expired
) VALUES (
    '77777777-7777-7777-7777-777777777777',
    'admin',
    'admin@bondtradex.com',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiEF4Nvqmo1JmG8EtKkP1gO8nFQwD3K',
    true,
    true,
    true,
    true
);

INSERT INTO user_roles (user_id, role_id) VALUES
(
    '77777777-7777-7777-7777-777777777777',
    '66666666-6666-6666-6666-666666666666'
);