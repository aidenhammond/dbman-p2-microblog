/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.services;

import java.util.List;
import java.util.ArrayList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.cs4370.models.FollowableUser;
import uga.menik.cs4370.utility.Utility;

import javax.sql.DataSource;
/**
 * This service contains people related functions.
 */
@Service
public class PeopleService {
    
    // dataSource enables talking to the database.
    private final DataSource dataSource;

    /**
     * See AuthInterceptor notes regarding dependency injection and
     * inversion of control.
     */
    @Autowired
    public PeopleService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * This function should query and return all users that 
     * are followable. The list should not contain the user 
     * with id userIdToExclude.
     */
    public List<FollowableUser> getFollowableUsers(String userIdToExclude) {

        List<FollowableUser> followableUsers = new ArrayList<>();


        // Write an SQL query to find the users that are not the current user (and added isFollowed).
        final String usersNotUserSql = """
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
        """;


	
        // Run the query with a datasource.
        // See UserService.java to see how to inject DataSource instance and
        // use it to run a query.
        try (Connection conn = dataSource.getConnection(); PreparedStatement userStmt = conn.prepareStatement(usersNotUserSql)) {

            userStmt.setString(1, userIdToExclude);
            userStmt.setString(2, userIdToExclude);


            try(ResultSet rs = userStmt.executeQuery()) {
                while(rs.next()) {
                    String userId = rs.getString("userId");
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");
                    boolean isFollowed = rs.getBoolean("isFollowed");

                    String lastActiveDate = rs.getTimestamp("lastActiveDate") != null ? rs.getTimestamp("lastActiveDate").toString() : "N/A";

                    
                    
                    FollowableUser user = new FollowableUser(userId, firstName, lastName, isFollowed, lastActiveDate);
                    followableUsers.add(user);
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return followableUsers;
        
    }

}
