INSERT INTO role_permissions (role_id, permission_id)
SELECT
    '66666666-6666-6666-6666-666666666666'::UUID,
    permission.id
FROM permissions permission
ON CONFLICT (role_id, permission_id) DO NOTHING;