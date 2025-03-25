/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.User;
import uga.menik.cs4370.utility.Utility;
import uga.menik.cs4370.services.UserService;

import javax.sql.DataSource;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.ResultSet;

import java.time.LocalDateTime;

import java.util.ArrayList;

/**
 * This controller handles the home page and some of it's sub URLs.
 */
@Controller
@RequestMapping
public class HomeController {

    private final UserService userService;
    private final DataSource dataSource;

    /**
     * Constructor; allows access to user information.
     */ 
    @Autowired
    public HomeController(
	UserService userService,
	DataSource dataSource
    ) {
        this.userService = userService;
	this.dataSource = dataSource;
    }


    /**
     * This is the specific function that handles the root URL itself.
     * 
     * Note that this accepts a URL parameter called error.
     * The value to this parameter can be shown to the user as an error message.
     * See notes in HashtagSearchController.java regarding URL parameters.
     */
    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) {
        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("home_page");

        // Following line populates sample data.
        // You should replace it with actual data from the database.
        //List<Post> posts = Utility.createSamplePostsListWithoutComments();
	List<Post> posts = new ArrayList<>();
        String sql = """
            SELECT p.postId, p.content, p.postDate, p.userId, p.heartsCount, p.commentsCount,
                   u.firstName, u.lastName
            FROM post p, user u
            where p.userId = u.userId
            ORDER BY p.postDate DESC
        """;
    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
    
            while (rs.next()) {
                String postId = String.valueOf(rs.getInt("postId"));
                String content = rs.getString("content");
                String postDate = rs.getTimestamp("postDate").toString();
                String userId = String.valueOf(rs.getInt("userId"));
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
    
        } catch (Exception e) {
            e.printStackTrace();
        }
        mv.addObject("posts", posts);

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        // An error message can be optionally specified with a url query parameter too.
        String errorMessage = error;
        mv.addObject("errorMessage", errorMessage);

        // Enable the following line if you want to show no content message.
        // Do that if your content list is empty.
        // mv.addObject("isNoContent", true);

        return mv;
    }

    /**
     * This function handles the /createpost URL.
     * This handles a post request that is going to be a form submission.
     * The form for this can be found in the home page. The form has a
     * input field with name = posttext. Note that the @RequestParam
     * annotation has the same name. This makes it possible to access the value
     * from the input from the form after it is submitted.
     */
    @PostMapping("/createpost")
    public String createPost(@RequestParam(name = "posttext") String postText) {
	if (!userService.isAuthenticated()) {
		String message = URLEncoder.encode("Failed to create the post. Please log in and try again.",
				StandardCharsets.UTF_8);
		return "redirect:/?error=" + message;
	}
        System.out.println("User is creating post: " + postText);
	String sql = "INSERT INTO post (userId, heartsCount, commentsCount, postDate, content) VALUES (?, 0, 0, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(userService.getLoggedInUser().getUserId()));
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(3, postText);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
		    return "redirect:/";
	    }
	    else {
		String message = URLEncoder.encode("Failed to create the post. Please try again.",
				StandardCharsets.UTF_8);
		return "redirect:/?error=" + message;
	    }

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/?error=";
        }

    }

}
