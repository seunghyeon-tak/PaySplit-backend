alter table subscriptions
    modify column started_at datetime;

alter table subscriptions
    modify column ended_at datetime;