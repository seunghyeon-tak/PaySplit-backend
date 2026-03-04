create table subscription_plans
(
    id          bigint primary key auto_increment,
    policy_id   bigint         not null comment '정산 정책',
    name        varchar(50)    not null,
    price       decimal(10, 2) not null,
    max_members int            not null,
    active      boolean        not null default true comment '신규 가입 가능 여부',
    created_at  datetime       not null default current_timestamp,

    constraint fk_subscription_plans_policy foreign key (policy_id)
        references settlement_policies (id) on delete restrict¬
);

create table subscriptions
(
    id           bigint primary key auto_increment,
    plan_id      bigint      not null,
    party_id     bigint      not null,
    status       varchar(20) not null comment 'ACTIVE / EXPIRED / CANCELED',
    started_at   datetime    not null comment '구독 시작일',
    ended_at     datetime    not null comment '구독 만료일',
    auto_renewal boolean     not null default false comment '자동 갱신 여부',
    created_at   datetime    not null default current_timestamp,

    constraint fk_subscriptions_plan foreign key (plan_id)
        references subscription_plans (id) on delete restrict,
    constraint fk_subscriptions_party foreign key (party_id)
        references parties (id) on delete restrict
);