insert into users (USER_ID, EMAIL, NICKNAME, PASSWORD, ROLE, USER_TYPE)
values (99990, 'admin@eco.com', 'admin1234', '{bcrypt}$2a$10$EwUz1xN6sDbOM6Ss40bLxO5j.EPmj9pbhL2oO92ZBJQ8zuXoRUPwG', 'ADMIN', 'DEAF');
insert into user_social (user_id,  user_social_id, social_type, social_id)
values (99990, 99990, 'ECO', 'eco_admin_99990');

insert into users (USER_ID, EMAIL, NICKNAME, PASSWORD, ROLE, USER_TYPE)
values (99991, 'friend1@eco.com', 'friend1', '{bcrypt}$2a$10$EwUz1xN6sDbOM6Ss40bLxO5j.EPmj9pbhL2oO92ZBJQ8zuXoRUPwG', 'ADMIN', 'DEAF');
insert into user_social (user_id, user_social_id, social_type, social_id)
values (99991, 99991, 'NAVER', 'naver_admin_99991');

insert into users (USER_ID, EMAIL, NICKNAME, PASSWORD, ROLE, USER_TYPE)
values (99992, 'friend2@eco.com', 'friend2', '{bcrypt}$2a$10$EwUz1xN6sDbOM6Ss40bLxO5j.EPmj9pbhL2oO92ZBJQ8zuXoRUPwG', 'ADMIN', 'DEAF');
insert into user_social (user_id, user_social_id, social_type, social_id)
values (99992, 99992, 'GOOGLE', 'google_admin_99992');

insert into users (USER_ID, EMAIL, NICKNAME, PASSWORD, ROLE, USER_TYPE)
values (99993, 'friend3@eco.com', 'friend3', '{bcrypt}$2a$10$EwUz1xN6sDbOM6Ss40bLxO5j.EPmj9pbhL2oO92ZBJQ8zuXoRUPwG', 'ADMIN', 'DEAF');
insert into user_social (user_id, user_social_id, social_type, social_id)
values (99993, 99993, 'KAKAO', 'kakako_admin_99993');

insert into users (USER_ID, EMAIL, NICKNAME, PASSWORD, ROLE, USER_TYPE)
values (99994, 'friend4@eco.com', 'friend4', '{bcrypt}$2a$10$EwUz1xN6sDbOM6Ss40bLxO5j.EPmj9pbhL2oO92ZBJQ8zuXoRUPwG', 'ADMIN', 'NONDEAF');
insert into user_social (user_id, user_social_id, social_type, social_id)
values (99994, 99994, 'ECO', 'eco_friend1_99994');

insert into users (USER_ID, EMAIL, NICKNAME, PASSWORD, ROLE, USER_TYPE)
values (99995, 'friend5@eco.com', 'friend5', '{bcrypt}$2a$10$EwUz1xN6sDbOM6Ss40bLxO5j.EPmj9pbhL2oO92ZBJQ8zuXoRUPwG', 'ADMIN', 'DEAF');
insert into user_social (user_id, user_social_id, social_type, social_id)
values (99995, 99995, 'ECO', 'eco_friend2_99995');


insert into friend_list (friend_list_id, friend_id, user_id)
values (99991, 99991, 99990);

insert into friend_list (friend_list_id, friend_id, user_id)
values (99992, 99990, 99991);

insert into friend_request_list (friend_request_list_id, friend_id, friend_state, user_id)
values (99991, 99991, 'APPROVED', 99990);

insert into friend_request_list (friend_request_list_id, friend_id, friend_state, user_id)
values (99992, 99992, 'SENDING', 99990);

insert into friend_request_list (friend_request_list_id, friend_id, friend_state, user_id)
values (99993, 99993, 'REMOVED', 99990);

insert into friend_request_list (friend_request_list_id, friend_id, friend_state, user_id)
values (99994, 99990, 'SENDING', 99994);

insert into friend_request_list (friend_request_list_id, friend_id, friend_state, user_id)
values (99995, 99990, 'SENDING', 99995);

insert into friend_request_notification (id, title, message, view, friend_request_list_id, request_user_id, receipt_user_id, notification_type, created_at)
values (99990, 'Example1', 'Dummy1', false, 99992, 99990, 99992, 'FRIEND_REQUEST', NOW());

insert into friend_request_notification (id, title, message, view, friend_request_list_id, request_user_id, receipt_user_id, notification_type, created_at)
values (99994, 'Example2', 'Dummy2', false, 99994, 99994, 99990, 'FRIEND_REQUEST', NOW());

insert into friend_request_notification (id, title, message, view, friend_request_list_id, request_user_id, receipt_user_id, notification_type, created_at)
values (99995, 'Example3', 'Dummy3', true, 99995, 99995, 99990, 'FRIEND_REQUEST', NOW());