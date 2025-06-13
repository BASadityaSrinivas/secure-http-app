-- :name create-users-table :! :raw
-- :doc Create characters table
create table users (
  id         serial not null,
  username   varchar primary key,
  password   varchar,
  created_at timestamp not null default current_timestamp
);


-- :name insert-user :! :n
-- :doc Insert new username and password
insert into users (username, password)
values (:username, :password)
on conflict do nothing;


-- :name get-user-by-username :? :n
-- :doc Fetch user data
select * from users
where username = :username;