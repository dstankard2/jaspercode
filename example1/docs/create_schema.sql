
create schema example1;
create user example1_user@localhost identified by 'welcome';
grant all privileges on example1.* TO 'example1_user'@'localhost';

ALTER USER 'example1_user'@'localhost' IDENTIFIED WITH mysql_native_password BY 'welcome';

