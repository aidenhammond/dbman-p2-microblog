-- Create the database.
create database if not exists cs4370_mb_platform;

-- Use the created database.
use cs4370_mb_platform;

-- Create the user table.
create table if not exists user (
    userId int auto_increment,
    username varchar(255) not null,
    password varchar(255) not null,
    firstName varchar(255) not null,
    lastName varchar(255) not null,
    primary key (userId),
    unique (username),
    constraint userName_min_length check (char_length(trim(userName)) >= 2),
    constraint firstName_min_length check (char_length(trim(firstName)) >= 2),
    constraint lastName_min_length check (char_length(trim(lastName)) >= 2)
);


-- Create the post table
create table if not exists post (
	postId int auto_increment,
	userId int not null,
	heartsCount int not null check (heartsCount >= 0),
	commentsCount int not null check (commentsCount >= 0),
	postDate datetime not null,
	content text not null,
	primary key (postId),
	foreign key (userId) references user(userId)
);

-- Hearted table
create table if not exists heart (
	userId int not null,
	postId int not null,
	foreign key (userId) references user(userId),
	foreign key (postId) references post(postId)
);

-- Bookmarked table
create table if not exists bookmark (
	userId int not null,
	postId int not null,
	foreign key (userId) references user(userId),
	foreign key (postId) references post(postId)
);

-- Create the comment table
create table if not exists comment (
	postId int not null,
	content text not null,
	postDate datetime not null,
	userId int not null,
	foreign key (postId) references post(postId),
	foreign key (userId) references user(userId)
);

-- create table for follows
create table if not exists follows (
        followerId int,
        followedId int,
        foreign key (followerId) references user(userId),
        foreign key (followedId) references user(userId)
);


-- Insert sample users
insert into user (username, password, firstName, lastName) values 
('show tab', '$2a$10$0Xrfo.n5O7ZVU322.xPwcu1IiZPYNDkegHds8K3zDUYuDdfUlHf0.', 'Aiden', 'Hammond'),
('jpetro123', '$2a$10$QmssvcnTGEHHI/lmCvN2leihEl1UCQwzhELFzfaplbEee9Z60y0Gq', 'Janet', 'Petro'),
('carol789', 'awjefiowjaoief', 'Carol', 'Jones'),
('dave321', 'eaiowjfowiaejfoawefa', 'Dave', 'Smith');

-- Insert sample posts
insert into post (userId, heartsCount, commentsCount, postDate, content) values 
(1, 5, 2, '2025-03-24 10:15:00', 'iowajfw.'),
(2, 3, 1, '2025-03-23 08:45:00', 'oweiafjowifjawoiefj.'),
(3, 10, 5, '2025-03-22 12:30:00', 'fiawjfoiajef.'),
(4, 0, 0, '2025-03-24 14:00:00', 'efoawjefioafweiofjao');

-- Insert sample hearts
insert into heart (userId, postId) values 
(1, 2),
(2, 1),
(3, 1),
(4, 3);

-- Insert sample bookmarks
insert into bookmark (userId, postId) values 
(1, 3),
(3, 2),
(4, 1);

-- Insert sample comments
insert into comment (postId, content, postDate, userId) values 
(1, 'sick!', '2025-03-24 11:00:00', 2),
(1, 'hiweafow', '2025-03-24 11:05:00', 3),
(2, 'weiofaj.', '2025-03-23 09:00:00', 1),
(3, 'eiwaofjwoijf', '2025-03-22 13:00:00', 4);

-- Insert sample follows
insert into follows (followerId, followedId) values 
(1, 2),
(1, 3),
(2, 1),
(4, 1),
(3, 4);

