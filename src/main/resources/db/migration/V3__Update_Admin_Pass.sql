insert into usr (id, active, password, username) values (1, true, '$2a$08$MleS93yHbEW95qtLxIqHhu3Q72kA7kHuJo7xbcjTcIZRw0f8JkiVq', 'admin');

insert into user_role (user_id, roles) values (1, 'USER'), (1, 'ADMIN');