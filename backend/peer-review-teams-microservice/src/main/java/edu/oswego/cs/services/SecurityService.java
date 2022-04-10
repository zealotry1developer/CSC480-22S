package edu.oswego.cs.services;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import org.bson.Document;
import edu.oswego.cs.requests.TeamParam;


public class SecurityService {

    /**
     * Checks if the student is in the right course
     * @param courseDocument Document
     * @param request TeamParam
     */
    public boolean isStudentValid(Document courseDocument,TeamParam request) {
        List<String> students = courseDocument.getList("students", String.class);

        for (String student : students) 
            if (request.getStudentID().equals(student)) return true;
        
        return false;
    }

    /**
     * Checks if a student is already in a team
     * @param teamCollection MongoCollection<Document>
     * @param request TeamParam:{"team_id", "course_id", "student_id", "team_size"}
     */
    public boolean isStudentAlreadyInATeam(MongoCollection<Document> teamCollection, TeamParam request) {
        MongoCursor<Document> cursor = teamCollection.find().iterator();
        if (cursor == null) 
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve team collection.").build());
        
        try { 
            while(cursor.hasNext()) { 
                Document teamDocument = cursor.next();
                List<String> members = teamDocument.getList("team_members", String.class);
                String courseID = teamDocument.get("course_id").toString();
                for (String member : members) {
                    if (request.getStudentID().equals(member)){
                        if (request.getCourseID().equals(courseID))
                            return true;
                    }
                }
            } 
        } finally { 
            cursor.close();
        } 
        return false;
    }

    /**
     * Checks if the team with the request.teamID() is already created
     * @param teamCollection MongoCollection<Document>
     * @param request TeamParam:{"team_id", "course_id", "student_id", "team_size"}
     */
    public boolean isTeamCreated(MongoCollection<Document> teamCollection, TeamParam request) {
        MongoCursor<Document> cursor = teamCollection.find().iterator();
        if (cursor == null) 
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve team collection.").build());
        
        try { 
            while(cursor.hasNext()) {
                Document teamDocument = cursor.next();
                String courseID = teamDocument.get("course_id").toString();
                String teamID = teamDocument.get("team_id").toString();
                
                if (request.getCourseID().equals(courseID) && request.getTeamID().equals(teamID))
                        return true;

            }
        } finally { 
            cursor.close();
        } 

        return false;
    }

   
}
