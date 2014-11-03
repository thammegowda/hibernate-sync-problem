create user 'hibuser'@'localhost' identified by 'hibpass';
create database hib_ex;
grant all privileges on hib_ex.* to 'hibuser'@'localhost' with grant option;
