INSERT INTO roles (id, name, description) VALUES
('11111111-1111-1111-1111-111111111111', 'CLIENT', 'Client user who can create IOI and place bids'),
('22222222-2222-2222-2222-222222222222', 'SALES', 'Sales team user who can review client IOIs'),
('33333333-3333-3333-3333-333333333333', 'TRADER', 'Trader who evaluates IOIs and selects winning bids'),
('44444444-4444-4444-4444-444444444444', 'SETTLEMENT_OPS', 'Settlement operations user'),
('55555555-5555-5555-5555-555555555555', 'DEV_SUPPORT', 'Dev support user for monitoring and support'),
('66666666-6666-6666-6666-666666666666', 'ADMIN', 'System administrator');

INSERT INTO permissions (id, name, description) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'IOI_CREATE', 'Create IOI'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'IOI_REVIEW', 'Review IOI'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'IOI_APPROVE', 'Approve IOI'),
('dddddddd-dddd-dddd-dddd-dddddddddddd', 'BID_CREATE', 'Create bid'),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'BID_SELECT', 'Select winning bid'),
('ffffffff-ffff-ffff-ffff-ffffffffffff', 'SETTLEMENT_MANAGE', 'Manage settlement'),
('99999999-9999-9999-9999-999999999999', 'MONITORING_VIEW', 'View monitoring data');

INSERT INTO role_permissions (role_id, permission_id) VALUES
('11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
('11111111-1111-1111-1111-111111111111', 'dddddddd-dddd-dddd-dddd-dddddddddddd'),

('22222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'),

('33333333-3333-3333-3333-333333333333', 'cccccccc-cccc-cccc-cccc-cccccccccccc'),
('33333333-3333-3333-3333-333333333333', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee'),

('44444444-4444-4444-4444-444444444444', 'ffffffff-ffff-ffff-ffff-ffffffffffff'),

('55555555-5555-5555-5555-555555555555', '99999999-9999-9999-9999-999999999999');