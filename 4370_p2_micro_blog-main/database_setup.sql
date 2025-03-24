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
