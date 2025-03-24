/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.services.UserService;
import uga.menik.cs4370.utility.Utility;
import uga.menik.cs4370.models.User;

import javax.sql.DataSource;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.ResultSet;

import java.time.LocalDateTime;

import java.util.ArrayList;
/**
 * Handles /profile URL and its sub URLs.
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    // UserService has user login and registration related functions.
    private final UserService userService;
    private final DataSource dataSource;

    /**
     * See notes in AuthInterceptor.java regarding how this works 
     * through dependency injection and inversion of control.
     */
    @Autowired
    public ProfileController(UserService userService, DataSource dataSource) {
        this.userService = userService;
	this.dataSource = dataSource;
    }

    /**
     * This function handles /profile URL itself.
     * This serves the webpage that shows posts of the logged in user.
     */
    @GetMapping
    public ModelAndView profileOfLoggedInUser() {
        System.out.println("User is attempting to view profile of the logged in user.");
        return profileOfSpecificUser(userService.getLoggedInUser().getUserId());
    }

    /**
     * This function handles /profile/{userId} URL.
     * This serves the webpage that shows posts of a speific user given by userId.
     * See comments in PeopleController.java in followUnfollowUser function regarding 
     * how path variables work.
     */
    @GetMapping("/{userId}")
    public ModelAndView profileOfSpecificUser(@PathVariable("userId") String userId) {
        System.out.println("User is attempting to view profile: " + userId);
        
        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("posts_page");
	List<Post> posts = new ArrayList<>();


	String sql = """
            SELECT p.postId, p.content, p.postDate, p.userId, p.heartsCount, p.commentsCount,
                   u.firstName, u.lastName
            FROM post p
            JOIN user u ON p.userId = u.userId
            WHERE p.userId = ?
            ORDER BY p.postDate DESC
        """;
    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            stmt.setInt(1, Integer.parseInt(userId));
    
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String postId = String.valueOf(rs.getInt("postId"));
                    String content = rs.getString("content");
                    String postDate = rs.getTimestamp("postDate").toString();
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");
    
                    boolean isHearted = userService.isPostHeartedByUser(postId);
                    boolean isBookmarked = userService.isPostBookmarkedByUser(postId);
    
                    User user = new User(userId, firstName, lastName);
                    Post post = new Post(postId, content, postDate, user,
                                         rs.getInt("heartsCount"),
                                         rs.getInt("commentsCount"),
                                         isHearted,
                                         isBookmarked);
                    posts.add(post);
                }
            }
    
        } catch (Exception e) {
            e.printStackTrace();
            mv.addObject("errorMessage", "Failed to load profile posts.");
        }
    
        mv.addObject("posts", posts);
        if (posts.isEmpty()) {
            mv.addObject("isNoContent", true);
        }


        // Following line populates sample data.
        // You should replace it with actual data from the database.
        //List<Post> posts = Utility.createSamplePostsListWithoutComments();
        //mv.addObject("posts", posts);

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        // String errorMessage = "Some error occured!";
        // mv.addObject("errorMessage", errorMessage);

        // Enable the following line if you want to show no content message.
        // Do that if your content list is empty.
        // mv.addObject("isNoContent", true);
        
        return mv;
    }
    
}
