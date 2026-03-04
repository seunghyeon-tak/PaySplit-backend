create table users
(
    id            bigint primary key auto_increment,
    name          varchar(50)  not null,
    email         varchar(100) null comment '연락용 이메일',
    withdrawal_at datetime     null,
    created_at    datetime     not null default current_timestamp
);

create table user_auth
(
    id         bigint primary key auto_increment,
    user_id    bigint       not null,
    provider   varchar(20)  not null comment 'LOCAL / GOOGLE / KAKAO 등',
    identifier varchar(255) not null comment '일반 로그인 : 이메일, 소셜 : oauth_id',
    password   varchar(100) null comment '일반 로그인일 때만 사용',
    active     boolean      not null default true,
    created_at datetime     not null default current_timestamp,

    unique (provider, identifier),
    constraint fk_user_auth_user foreign key (user_id) references users (id) on delete restrict
);