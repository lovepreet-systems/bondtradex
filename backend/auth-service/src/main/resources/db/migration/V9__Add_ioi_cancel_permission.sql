INSERT INTO permissions (
    id,
    name,
    description
) VALUES (
             '77777777-dddd-4444-eeee-777777777777',
             'IOI_CANCEL',
             'Cancel an eligible IOI'
         )
    ON CONFLICT (name) DO NOTHING;

INSERT INTO role_permissions (
    role_id,
    permission_id
) VALUES (
             '66666666-6666-6666-6666-666666666666',
             '77777777-dddd-4444-eeee-777777777777'
         )
    ON CONFLICT (role_id, permission_id) DO NOTHING;