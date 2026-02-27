alter table settlements add constraint uk_settlement_payment_type unique (payment_id, type);

alter table settlements drop index uk_settlement_payment;