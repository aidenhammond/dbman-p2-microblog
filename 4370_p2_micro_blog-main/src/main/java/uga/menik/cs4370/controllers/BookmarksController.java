/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.utility.Utility;
import uga.menik.cs4370.models.User;
import uga.menik.cs4370.services.UserService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Handles /bookmarks and its sub URLs.
 * No other URLs at this point.
 * 
 * Learn more about @Controller here: 
 * https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
 */
@Controller
@RequestMapping("/bookmarks")
public class BookmarksController {

    private final UserService userService;
    private final DataSource dataSource;

    @Autowired
    public BookmarksController(UserService userService, DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    /**
     * /bookmarks URL itself is handled by this.
     */
    @GetMapping
    public ModelAndView webpage() {
        // posts_page is a mustache template from src/main/resources/templates.
        // ModelAndView class enables initializing one and populating placeholders
        // in the template using Java objects assigned to named properties.
        ModelAndView mv = new ModelAndView("posts_page");

        // Following line populates sample data.
        // You should replace it with actual data from the database.
        //List<Post> posts = Utility.createSamplePostsListWithoutComments();

        List<Post> posts = new ArrayList<>();
        String sql = """
            SELECT p.postId, p.content, p.postDate, p.userId, p.heartsCount, p.commentsCount,
                   u.firstName, u.lastName
            FROM post p, user u, bookmark b
            WHERE p.userId = u.userId
              AND p.postId = b.postId
              AND b.userId = ?
            ORDER BY p.postDate DESC
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(userService.getLoggedInUser().getUserId()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String postId = String.valueOf(rs.getInt("postId"));
                    String content = rs.getString("content");
                    String userId = String.valueOf(rs.getInt("userId"));
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");

                    Timestamp ts = rs.getTimestamp("postDate");
                    String postDate = ts.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a"));

                    boolean isHearted = userService.isPostHeartedByUser(postId);
                    boolean isBookmarked = true; // all these posts are already bookmarked

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
            mv.addObject("errorMessage", "Failed to load bookmarks.");
        }

        mv.addObject("posts", posts);

        if (posts.isEmpty()) {
            mv.addObject("isNoContent", true);
        }

        return mv;
    }
    
}
