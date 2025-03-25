/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.utility.Utility;
import uga.menik.cs4370.models.User;
import uga.menik.cs4370.services.UserService;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.ResultSet;

import java.time.LocalDateTime;

import java.util.ArrayList;
/**
 * Handles /hashtagsearch URL and possibly others.
 * At this point no other URLs.
 */
@Controller
@RequestMapping("/hashtagsearch")
public class HashtagSearchController {


    private final UserService userService;
    private final DataSource dataSource;
    
    @Autowired
    public HashtagSearchController(UserService userService, DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    /**
     * This function handles the /hashtagsearch URL itself.
     * This URL can process a request parameter with name hashtags.
     * In the browser the URL will look something like below:
     * http://localhost:8081/hashtagsearch?hashtags=%23amazing+%23fireworks
     * Note: the value of the hashtags is URL encoded.
     */
    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "hashtags") String hashtags) {
        System.out.println("User is searching: " + hashtags);

        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("posts_page");
	List<Post> posts = new ArrayList<>();

        // Following line populates sample data.
        // You should replace it with actual data from the database.
        //List<Post> posts = Utility.createSamplePostsListWithoutComments();
        //mv.addObject("posts", posts);
	// Clean + split hashtags
        String[] hashtagArray = hashtags.trim().split("\\s+");  // split on spaces
    
        if (hashtagArray.length == 0) {
            mv.addObject("errorMessage", "No hashtags provided.");
            return mv;
        }
    
        // Build SQL with OR clauses
        StringBuilder sqlBuilder = new StringBuilder("""
            SELECT p.postId, p.content, p.postDate, p.userId, p.heartsCount, p.commentsCount,
                   u.firstName, u.lastName
            FROM post p
            JOIN user u ON p.userId = u.userId
            WHERE
        """);
    
        for (int i = 0; i < hashtagArray.length; i++) {
            if (i > 0) sqlBuilder.append(" OR ");
            sqlBuilder.append("p.content LIKE ?");
        }
        sqlBuilder.append(" ORDER BY p.postDate DESC");
    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
    
            for (int i = 0; i < hashtagArray.length; i++) {
                String tag = hashtagArray[i].trim().replace("#", "");
                stmt.setString(i + 1, "%#" + tag + "%");  // match #hashtag anywhere in content
            }
    
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String postId = String.valueOf(rs.getInt("postId"));
                    String content = rs.getString("content");
                    String postDate = rs.getTimestamp("postDate").toString();
                    String userId = String.valueOf(rs.getInt("userId"));
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");
    
                    // Checking if post is hearted or bookmarked for particular user
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
            mv.addObject("errorMessage", "Failed to load posts for hashtags.");
        }
    
        // Checking if post has zero content (empty)
        mv.addObject("posts", posts);
        if (posts.isEmpty()) {
            mv.addObject("isNoContent", true);
        }

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
