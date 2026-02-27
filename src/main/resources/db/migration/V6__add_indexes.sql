create index idx_payments_payer_id on payments (payer_id);
create index idx_payments_policy_id on payments (policy_id);
create index idx_payments_status on payments (status);
create index idx_payments_external_payment_id on payments (external_payment_id);

create index idx_settlements_policy_id on settlements (policy_id);
create index idx_settlements_status on settlements (status);
create index idx_settlements_original_id on settlements (original_settlement_id);

create index idx_settlement_items_receiver on settlement_items (receiver_type, receiver_id);
