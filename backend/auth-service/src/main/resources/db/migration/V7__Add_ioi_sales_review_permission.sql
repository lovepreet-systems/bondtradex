INSERT INTO permissions (
    id,
    name,
    description
) VALUES (
             '77777777-bbbb-4444-cccc-777777777777',
             'IOI_SALES_REVIEW',
             'Start and complete sales review for an IOI'
         )
    ON CONFLICT (name) DO NOTHING;

INSERT INTO role_permissions (
    role_id,
    permission_id
) VALUES (
             '66666666-6666-6666-6666-666666666666',
             '77777777-bbbb-4444-cccc-777777777777'
         )
    ON CONFLICT (role_id, permission_id) DO NOTHING;