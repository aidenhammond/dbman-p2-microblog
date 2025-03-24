/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.FollowableUser;
import uga.menik.cs4370.services.PeopleService;
import uga.menik.cs4370.services.UserService;
import uga.menik.cs4370.utility.Utility;

import javax.sql.DataSource;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.ResultSet;
/**
 * Handles /people URL and its sub URL paths.
 */
@Controller
@RequestMapping("/people")
public class PeopleController {

    // Inject UserService and PeopleService instances.
    // See LoginController.java to see how to do this.
    // Hint: Add a constructor with @Autowired annotation.
    //
    private final PeopleService peopleService;
    private final UserService userService;
    private final DataSource dataSource;

    /**
     * Constructor-based dependency injection for PeopleService and UserService.
     */
    @Autowired
    public PeopleController(PeopleService peopleService, UserService userService, DataSource dataSource) {
        this.peopleService = peopleService;
        this.userService = userService;
	this.dataSource = dataSource;
    }


    /**
     * Serves the /people web page.
     * 
     * Note that this accepts a URL parameter called error.
     * The value to this parameter can be shown to the user as an error message.
     * See notes in HashtagSearchController.java regarding URL parameters.
     */
    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) {
        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("people_page");

	String curr_user_id = userService.getLoggedInUser().getUserId();

        // Following line populates sample data.
        // You should replace it with actual data from the database.
        // Use the PeopleService instance to find followable users.
        // Use UserService to access logged in userId to exclude.
        List<FollowableUser> followableUsers = peopleService.getFollowableUsers(curr_user_id);//Utility.createSampleFollowableUserList();
        mv.addObject("users", followableUsers);

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
     * This function handles user follow and unfollow.
     * Note the URL has parameters defined as variables ie: {userId} and {isFollow}.
     * Follow and unfollow is handled by submitting a get type form to this URL 
     * by specifing the userId and the isFollow variables.
     * Learn more here: https://www.w3schools.com/tags/att_form_method.asp
     * An example URL that is handled by this function looks like below:
     * http://localhost:8081/people/1/follow/false
     * The above URL assigns 1 to userId and false to isFollow.
     */
    @GetMapping("{userId}/follow/{isFollow}")
    public String followUnfollowUser(@PathVariable("userId") String userId,
            @PathVariable("isFollow") Boolean isFollow) {
        System.out.println("User is attempting to follow/unfollow a user:");
        System.out.println("\tuserId: " + userId);
        System.out.println("\tisFollow: " + isFollow);

        // Redirect the user if the comment adding is a success.
        // return "redirect:/people";
	String followerId = userService.getLoggedInUser().getUserId();

        String sql;
        if (isFollow) {
            sql = "INSERT INTO follows (followerId, followedId) VALUES (?, ?)";
        } else {
            sql = "DELETE FROM follows WHERE followerId = ? AND followedId = ?";
        }
    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            stmt.setInt(1, Integer.parseInt(followerId));
            stmt.setInt(2, Integer.parseInt(userId));
    
            int affectedRows = stmt.executeUpdate();
    
            if (affectedRows > 0) {
                return "redirect:/people";
            } else {
                String message = URLEncoder.encode("Failed to update follow status. Please try again.",
                                                   StandardCharsets.UTF_8);
                return "redirect:/people?error=" + message;
            }
    
        } catch (Exception e) {
            e.printStackTrace();
            String message = URLEncoder.encode("Failed to (un)follow the user. Please try again.",
                                               StandardCharsets.UTF_8);
            return "redirect:/people?error=" + message;
        }

        // Redirect the user with an error message if there was an error.
        //String message = URLEncoder.encode("Failed to (un)follow the user. Please try again.",
        //        StandardCharsets.UTF_8);
        //return "redirect:/people?error=" + message;
    }

}
