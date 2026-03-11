alter table subscription_plans
    add column platform_id bigint null after policy_id;

alter table subscription_plans
    add constraint fk_subscription_plans_platform
        foreign key (platform_id) references platforms (id) on delete restrict;