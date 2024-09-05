insert into users (USER_ID, EMAIL, NICKNAME, PASSWORD, ROLE, USER_TYPE)
values (99990, 'admin1234', 'admin1234', '{bcrypt}$2a$10$EwUz1xN6sDbOM6Ss40bLxO5j.EPmj9pbhL2oO92ZBJQ8zuXoRUPwG', 'USER', 'DEAF');

insert into users (USER_ID, EMAIL, NICKNAME, PASSWORD, ROLE, USER_TYPE)
values (99991, 'friend1', 'friend1', '{bcrypt}$2a$10$EwUz1xN6sDbOM6Ss40bLxO5j.EPmj9pbhL2oO92ZBJQ8zuXoRUPwG', 'USER', 'DEAF');

insert into users (USER_ID, EMAIL, NICKNAME, PASSWORD, ROLE, USER_TYPE)
values (99992, 'friend2', 'friend2', '{bcrypt}$2a$10$EwUz1xN6sDbOM6Ss40bLxO5j.EPmj9pbhL2oO92ZBJQ8zuXoRUPwG', 'USER', 'DEAF');

insert into users (USER_ID, EMAIL, NICKNAME, PASSWORD, ROLE, USER_TYPE)
values (99993, 'friend3', 'friend3', '{bcrypt}$2a$10$EwUz1xN6sDbOM6Ss40bLxO5j.EPmj9pbhL2oO92ZBJQ8zuXoRUPwG', 'USER', 'DEAF');

insert into users (USER_ID, EMAIL, NICKNAME, PASSWORD, ROLE, USER_TYPE)
values (99994, 'friend4', 'friend4', '{bcrypt}$2a$10$EwUz1xN6sDbOM6Ss40bLxO5j.EPmj9pbhL2oO92ZBJQ8zuXoRUPwG', 'USER', 'NONDEAF');

insert into friend_list (friend_list_id, friend_id, user_id)
values (99991, 99991, 99990);

insert into friend_list (friend_list_id, friend_id, user_id)
values (99992, 99990, 99991);

insert into friend_list (friend_list_id, friend_id, user_id)
values (99993, 99994, 99990);

insert into friend_list (friend_list_id, friend_id, user_id)
values (99994, 99990, 99994);

insert into friend_request_list (friend_request_list_id, friend_id, friend_state, user_id)
values (99991, 99991, 'APPROVED', 99990);

insert into friend_request_list (friend_request_list_id, friend_id, friend_state, user_id)
values (99992, 99992, 'SENDING', 99990);

insert into friend_request_list (friend_request_list_id, friend_id, friend_state, user_id)
values (99993, 99993, 'REMOVED', 99990);

insert into friend_request_list (friend_request_list_id, friend_id, friend_state, user_id)
values (99994, 99994, 'APPROVED', 99990);
