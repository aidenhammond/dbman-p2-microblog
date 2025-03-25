/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.ExpandedPost;
import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.Comment;
import uga.menik.cs4370.models.User;
import uga.menik.cs4370.utility.Utility;
import uga.menik.cs4370.services.UserService;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.ResultSet;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.ZoneId;
/**
 * Handles /post URL and its sub urls.
 */
@Controller
@RequestMapping("/post")
public class PostController {

    private final UserService userService;
    private final DataSource dataSource;

    /**
     * Constructor; allows access to user information.
     */ 
    @Autowired
    public PostController(
	UserService userService,
	DataSource dataSource
    ) {
        this.userService = userService;
	this.dataSource = dataSource;
    }


    /**
     * This function handles the /post/{postId} URL.
     * This handlers serves the web page for a specific post.
     * Note there is a path variable {postId}.
     * An example URL handled by this function looks like below:
     * http://localhost:8081/post/1
     * The above URL assigns 1 to postId.
     * 
     * See notes from HomeController.java regardig error URL parameter.
     */
    @GetMapping("/{postId}")
    public ModelAndView webpage(@PathVariable("postId") String postId,
            @RequestParam(name = "error", required = false) String error) {
        System.out.println("The user is attempting to view post with id: " + postId);

        ModelAndView mv = new ModelAndView("posts_page");

	List<ExpandedPost> posts = new ArrayList<>();

	List<Comment> commentsForPost = new ArrayList<>();
	String sql = """
		select c.content, c.postDate, c.userId, u.firstName, u.lastName
		from user u, comment c
		where c.userId = u.userId and c.postId = ?
		order by c.postDate asc
	""";
	try (Connection conn = dataSource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
		pstmt.setString(1, postId.toString());
		ResultSet rs = pstmt.executeQuery();
		while (rs.next()) {
			String userId = String.valueOf(rs.getInt("userId"));
			String firstName = rs.getString("firstName");
			String lastName = rs.getString("lastName");
			User user = new User(userId, firstName, lastName);

			String commentContent = rs.getString("content");
			Timestamp ts = rs.getTimestamp("postDate");
			String commentPostDate = ts.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime()
				.format(DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a"));
			commentsForPost.add(new Comment(userId, commentContent, commentPostDate, user));
		}
        } catch (Exception e) {
            e.printStackTrace();
        }
	sql = """
		select p.userId, p.content, p.postDate, u.firstName, u.lastName, p.heartsCount, p.commentsCount
		from post p, user u
		where p.userId = u.userId and p.postId = ?
	""";

	try (Connection conn = dataSource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
		pstmt.setString(1, postId);

		try (ResultSet rs = pstmt.executeQuery()) {
			rs.next();
			String userId = String.valueOf(rs.getInt("userId"));
			String firstName = rs.getString("firstName");
			String lastName = rs.getString("lastName");
			User user = new User(userId, firstName, lastName);
			
			boolean isHearted = userService.isPostHeartedByUser(postId);
			boolean isBookmarked = userService.isPostBookmarkedByUser(postId);

			String content = rs.getString("content");
			int heartsCount = rs.getInt("heartsCount");
			int commentsCount = rs.getInt("commentsCount");

			Timestamp ts = rs.getTimestamp("postDate");
			String postDate = ts.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime()
				.format(DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a"));

			posts.add(new ExpandedPost(postId, content, postDate, user, heartsCount, commentsCount, isHearted, isBookmarked, commentsForPost));

		} catch (Exception e) {
		    e.printStackTrace();
		}
	} catch (Exception e) {
	    e.printStackTrace();
	}
        mv.addObject("posts", posts);

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        // An error message can be optionally specified with a url query parameter too.
        String errorMessage = error;
        mv.addObject("errorMessage", errorMessage);

        return mv;
    }

    /**
     * Handles comments added on posts.
     * See comments on webpage function to see how path variables work here.
     * This function handles form posts.
     * See comments in HomeController.java regarding form submissions.
     */
    @PostMapping("/{postId}/comment")
    public String postComment(@PathVariable("postId") String postId,
            @RequestParam(name = "comment") String comment) {
        System.out.println("The user is attempting add a comment:");
        System.out.println("\tpostId: " + postId);
        System.out.println("\tcomment: " + comment);

	String addCommentSql = "insert into comment (postId, content, postDate, userId) values (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement addCommentStmt = conn.prepareStatement(addCommentSql)) {
	    

            addCommentStmt.setInt(1, Integer.parseInt(postId));
            addCommentStmt.setString(2, comment);
            addCommentStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            addCommentStmt.setInt(4, Integer.parseInt(userService.getLoggedInUser().getUserId()));

            // Execute the statement and check if rows are affected.
            int rowsAffected = addCommentStmt.executeUpdate();
	    if (rowsAffected > 0) {
		    String updatePostCommentCountSql = "update post set commentsCount = commentsCount + 1 where postId = ?";
		    PreparedStatement updatePostCommentCountStmt = conn.prepareStatement(updatePostCommentCountSql);
		    updatePostCommentCountStmt.setInt(1, Integer.parseInt(postId));
		    updatePostCommentCountStmt.executeUpdate();
		    // Redirect the user if the comment adding is a success.
		    // return "redirect:/post/" + postId;
		    return "redirect:/post/" + postId;
	    } else {
		// Redirect the user with an error message if there was an error.
		String message = URLEncoder.encode("Failed to post the comment. Please try again.",
			StandardCharsets.UTF_8);
		return "redirect:/post/" + postId + "?error=" + message;
	    }
        }
	catch(Exception e) {
		e.printStackTrace();
		// Redirect the user with an error message if there was an error.
		String message = URLEncoder.encode("Failed to post the comment. Please try again.",
			StandardCharsets.UTF_8);
		return "redirect:/post/" + postId + "?error=" + message;
	}
    }

    /**
     * Handles likes added on posts.
     * See comments on webpage function to see how path variables work here.
     * See comments in PeopleController.java in followUnfollowUser function regarding 
     * get type form submissions and how path variables work.
     */
	@GetMapping("/{postId}/heart/{isAdd}")
	public String addOrRemoveHeart(@PathVariable("postId") String postId,
            @PathVariable("isAdd") Boolean isAdd) {
      System.out.println("The user is attempting add or remove a heart:");
      System.out.println("\tpostId: " + postId);
		System.out.println("\tisAdd: " + isAdd);

	
      String updatePostHeartCountSql = "update post set heartsCount = heartsCount + 1 where postId = ?";
		String updateHeartSql = "insert into heart (userId, postId) values (?, ?)";
		if (!isAdd) {
			updatePostHeartCountSql = "update post set heartsCount = heartsCount - 1 where postId = ?";
			updateHeartSql = "delete from heart where userId = ? and postId = ?";
		}

		try (Connection conn = dataSource.getConnection();
				PreparedStatement updatePostHeartCountStmt = conn.prepareStatement(updatePostHeartCountSql);
				PreparedStatement updateHeartStmt = conn.prepareStatement(updateHeartSql)) {
			updatePostHeartCountStmt.setInt(1, Integer.parseInt(postId));
			int rowsAffected = updatePostHeartCountStmt.executeUpdate();

			updateHeartStmt.setInt(2, Integer.parseInt(postId));
			updateHeartStmt.setInt(1, Integer.parseInt(userService.getLoggedInUser().getUserId()));
			rowsAffected += updateHeartStmt.executeUpdate();

			System.out.println("Total number of rows affected: " + rowsAffected);

			
			if (rowsAffected > 1) {
				return "redirect:/post/" + postId;
			}
			else {
				String message = URLEncoder.encode("Failed to (un)like the post. Please try again.",
					StandardCharsets.UTF_8);
				return "redirect:/post/" + postId + "?error=" + message;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			String message = URLEncoder.encode("Failed to (un)like the post. Please try again.",
				StandardCharsets.UTF_8);
			return "redirect:/post/" + postId + "?error=" + message;
		}
    }

    /**
     * Handles bookmarking posts.
     * See comments on webpage function to see how path variables work here.
     * See comments in PeopleController.java in followUnfollowUser function regarding 
     * get type form submissions.
     */
	@GetMapping("/{postId}/bookmark/{isAdd}")
	public String addOrRemoveBookmark(@PathVariable("postId") String postId,
            @PathVariable("isAdd") Boolean isAdd) {
      System.out.println("The user is attempting add or remove a bookmark:");
      System.out.println("\tpostId: " + postId);
      System.out.println("\tisAdd: " + isAdd);

		String updateBookmarkSql = "insert into bookmark (userId, postId) values (?, ?)";
		if (!isAdd) {
			updateBookmarkSql = "delete from bookmark where userId = ? and postId = ?";
		}

		try (Connection conn = dataSource.getConnection();
				PreparedStatement updateBookmarkStmt = conn.prepareStatement(updateBookmarkSql)) {

			updateBookmarkStmt.setInt(2, Integer.parseInt(postId));
			updateBookmarkStmt.setInt(1, Integer.parseInt(userService.getLoggedInUser().getUserId()));
			int rowsAffected = updateBookmarkStmt.executeUpdate();

			System.out.println("Total number of rows affected: " + rowsAffected);
			
			if (rowsAffected > 0) {
				return "redirect:/post/" + postId;
			}
			else {
				String message = URLEncoder.encode("Failed to (un)bookmark the post. Please try again.",
					StandardCharsets.UTF_8);
				return "redirect:/post/" + postId + "?error=" + message;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			String message = URLEncoder.encode("Failed to (un)bookmark the post. Please try again.",
				StandardCharsets.UTF_8);
			return "redirect:/post/" + postId + "?error=" + message;
		}

   }

}
