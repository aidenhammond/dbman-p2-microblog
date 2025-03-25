--Finding posts that are bookmarked by the user. 
--http://localhost:8081/bookmarks

SELECT p.postId, p.content, p.postDate, p.userId, p.heartsCount, p.commentsCount,
                   u.firstName, u.lastName
            FROM post p, user u, bookmark b
            WHERE p.userId = u.userId
              AND p.postId = b.postId
              AND b.userId = ?
            ORDER BY p.postDate DESC


-- Used to gather posts with certain hashtag on them. Snippet from code given below such that can see how the query is building. Finds hashtag in within content of the post and then adds 
-- that post to hashtag search page. 
--http://localhost:8081/hashtagsearch?hashtags=test

SELECT p.postId, p.content, p.postDate, p.userId, p.heartsCount, p.commentsCount,
                   u.firstName, u.lastName
            FROM post p
            JOIN user u ON p.userId = u.userId
            WHERE
            
            ---for (int i = 0; i < hashtagArray.length; i++) {
            ---   if (i > 0) sqlBuilder.append(" OR ");
            ---   sqlBuilder.append("p.content LIKE ?");
            ---}
            ---sqlBuilder.append(" ORDER BY p.postDate DESC");
            
            ---try (Connection conn = dataSource.getConnection();
            --- PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
    
            ---for (int i = 0; i < hashtagArray.length; i++) {
            ---    String tag = hashtagArray[i].trim().replace("#", "");
            ---    stmt.setString(i + 1, "%#" + tag + "%");  // match #hashtag anywhere in content
            ---}

-- Finds all posts from all users and displays them on the profile page. Will list them by datetime. 
--http://localhost:8081/profile/14
SELECT p.postId, p.content, p.postDate, p.userId, p.heartsCount, p.commentsCount,
                   u.firstName, u.lastName
            FROM post p, user u
            where p.userId = u.userId
            ORDER BY p.postDate DESC

-- Adding new entry to follows table if user is found to follow someone. Either the first or second query is used depending on the following status. 
--http://localhost:8081/
INSERT INTO follows (followerId, followedId) VALUES (?, ?)
DELETE FROM follows WHERE followerId = ? AND followedId = ?

-- From PostController. Sorting comments from users on a post by datetime.  
--http://localhost:8081/post/5
select c.content, c.postDate, c.userId, u.firstName, u.lastName
		from user u, comment c
		where c.userId = u.userId and c.postId = ?
		order by c.postDate asc


-- From PostController. Queries for expanded post.
--http://localhost:8081/post/5
select p.userId, p.content, p.postDate, u.firstName, u.lastName, p.heartsCount, p.commentsCount
		from post p, user u
		where p.userId = u.userId and p.postId = ?


-- From PostController. Queries to allow for adding or removing hearts from a post. 
--http://localhost:8081/post/6
update post set heartsCount = heartsCount + 1 where postId = ?
insert into heart (userId, postId) values (?, ?)
update post set heartsCount = heartsCount - 1 where postId = ?
delete from heart where userId = ? and postId = ?

-- From ProfileController. Queries for displaying posts by a specific user and ordering them by datetime. 
--http://localhost:8081/profile
SELECT p.postId, p.content, p.postDate, p.userId, p.heartsCount, p.commentsCount,
                   u.firstName, u.lastName
            FROM post p, user u
            WHERE p.userId = ? and p.userId = u.userId
            ORDER BY p.postDate DESC


--From PeopleService. Finds users that are not the current user, option to follow each user displayed. 
--http://localhost:8081/people
SELECT u.userId, u.firstName, u.lastName,
                   CASE WHEN f.followerId IS NOT NULL THEN true ELSE false END AS isFollowed,
                   MAX(p.postDate) AS lastActiveDate
            FROM user u
            LEFT JOIN follows f 
                   ON u.userId = f.followedId AND f.followerId = ?
            LEFT JOIN post p 
                   ON u.userId = p.userId
            WHERE u.userId != ?
            GROUP BY u.userId, u.firstName, u.lastName, f.followerId
            ORDER BY lastActiveDate is not NULL, lastActiveDate DESC

--From userService. Stores user obect. 
--http://localhost:8081/profile
select * from user where username = ?

--From user service, finding if post is hearted by user. 
--http://localhost:8081/post/6
SELECT 1 FROM heart WHERE postId = ? AND userId = ? LIMIT 1

--From user service, finding is post is bookmarked by the user. 
--http://localhost:8081/post/6
SELECT 1 FROM bookmark WHERE postId = ? AND userId = ? LIMIT 1

--From user service, registering new user if was succcessfull. 
--http://localhost:8081/profile
insert into user (username, password, firstName, lastName) values (?, ?, ?, ?)

