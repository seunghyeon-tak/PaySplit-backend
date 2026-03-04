create table parties
(
    id          bigint primary key auto_increment,
    leader_id   bigint      not null comment '파티장 user_id',
    status      varchar(20) not null comment 'RECRUITING / ACTIVE / DISBANDED',
    invite_code varchar(50) null unique comment '초대 코드',
    created_at  datetime    not null default current_timestamp,


    constraint fk_paries_leader foreign key (leader_id) references users (id) on delete restrict
);

create table party_members
(
    id         bigint primary key auto_increment,
    party_id   bigint      not null,
    user_id    bigint      not null,
    status     varchar(20) not null comment 'ACTIVE / LEFT / KICKED',
    created_at datetime    not null default current_timestamp,


    unique (party_id, user_id),
    constraint fk_party_members_partie foreign key (party_id) references parties (id) on delete restrict,
    constraint fk_party_members_user foreign key (user_id) references users (id) on delete restrict
);