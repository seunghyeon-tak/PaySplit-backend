create table platforms
(
    id         bigint primary key auto_increment,
    name       varchar(50) not null unique,
    active     boolean     not null default true,
    created_at datetime    not null default current_timestamp
);

alter table subscription_plans
    add column platform_id bigint not null comment '플랫폼' after policy_id,
    add constraint fk_subscription_plans_platform
        foreign key (platform_id) references platforms (id) on delete restrict;