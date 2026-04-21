-- 주문 ID (토스페이먼츠에서 필요)
alter table payments add column order_id varchar(64) null comment '주문 ID' after external_payment_id;

-- 결제 승인 시간
alter table payments add column approved_at datetime null comment '결제 승인 시간' after settled_at;
