INSERT INTO permissions (
    id,
    name,
    description
) VALUES (
             '88888888-8888-8888-8888-888888888888',
             'IOI_UPDATE',
             'Update a draft IOI'
         )
    ON CONFLICT (name) DO NOTHING;

INSERT INTO role_permissions (
    role_id,
    permission_id
) VALUES (
             '66666666-6666-6666-6666-666666666666',
             '88888888-8888-8888-8888-888888888888'
         )
    ON CONFLICT (role_id, permission_id) DO NOTHING;