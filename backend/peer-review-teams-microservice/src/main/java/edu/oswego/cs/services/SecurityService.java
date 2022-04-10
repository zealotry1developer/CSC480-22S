package edu.oswego.cs.services;

import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import edu.oswego.cs.requests.SwitchTeamParam;
import edu.oswego.cs.requests.TeamParam;
import static com.mongodb.client.model.Filters.eq;


public class SecurityService {
    /**
     * Checks if the passed in studentID is in the right course
     * @param courseDocument Document
     * @param request TeamParam
     * @return boolean
     */
    public boolean isStudentValid(Document courseDocument, String studentID ) {
        List<String> students = courseDocument.getList("students", String.class);
        for (String student : students) 
            if (studentID.equals(student)) return true;
        
        return false;
    }

    /**
     * Checks if the passed in studentID is already in a team
     * @param teamCollection MongoCollection<Document>
     * @param request TeamParam:{"team_id", "course_id", "student_id", "team_size"}
     * @return boolean
     */
    public boolean isStudentAlreadyInATeam(MongoCollection<Document> teamCollection, String studentID, String courseID) {
        MongoCursor<Document> cursor = teamCollection.find().iterator();
        if (cursor == null) 
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve team collection.").build());
        
        try { 
            while(cursor.hasNext()) { 
                Document teamDocument = cursor.next();
                List<String> members = teamDocument.getList("team_members", String.class);
                String teamDocumentCourseID = teamDocument.get("course_id").toString();
                for (String member : members) {
                    if (studentID.equals(member)){
                        if (courseID.equals(teamDocumentCourseID))
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
     * Checks if the student is in the current team
     * @param teamCollection
     * @param teamID
     * @param studentID
     * @param courseID
     * @return boolean
     */
    public boolean isStudentInThisTeam(MongoCollection<Document> teamCollection, String teamID, String studentID, String courseID ) {
        Document teamDocument = teamCollection.find(eq("team_id", teamID)).first();
        List<String> members = teamDocument.getList("team_members", String.class);
        String teamDocumentCourseID = teamDocument.get("course_id").toString();
        for (String member : members) 
            if (studentID.equals(member) && courseID.equals(teamDocumentCourseID))
                return true;
        return false;
    }

    /**
     * Checks if the team is already full
     * @param teamCollection
     * @param teamID
     * @param courseID
     * @return boolean
     */
    public boolean isTeamFull(MongoCollection<Document> teamCollection, String teamID, String courseID ) {
        Document teamDocument = teamCollection.find(eq("team_id", teamID)).first();
        String teamDocumentCourseID = teamDocument.get("course_id").toString();
        if (teamDocument.getBoolean("is_full") && courseID.equals(teamDocumentCourseID)) 
            return true;
        return false;
    }

    /**
     * Checks if the team with the request.teamID() is already created
     * @param teamCollection MongoCollection<Document>
     * @param request TeamParam:{"team_id", "course_id", "student_id", "team_size"}
     * @return boolean
     */
    public boolean isTeamCreated(MongoCollection<Document> teamCollection, String teamID, String courseID) {
        MongoCursor<Document> cursor = teamCollection.find().iterator();
        if (cursor == null) 
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve team collection.").build());
        
        try { 
            while(cursor.hasNext()) {
                Document teamDocument = cursor.next();
                String teamDocumentCourseID = teamDocument.get("course_id").toString();
                String teamDocumentTeamID = teamDocument.get("team_id").toString();
                
                if (courseID.equals(teamDocumentCourseID) && teamID.equals(teamDocumentTeamID))
                        return true;
            }
        } finally { 
            cursor.close();
        } 
        return false;
    }

    /**
     * allows security checks on passed in information
     * @param courseDocument
     * @param request 
     * @param mode
     */
    public void securityChecks(MongoCollection<Document> teamCollection, Document courseDocument, TeamParam request, String mode) {
        if (!isStudentValid(courseDocument, request.getStudentID())) 
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Student not found in this course.").build());

        MongoCursor<Document> cursor = teamCollection.find().iterator();
        if (cursor == null) 
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve team collection.").build());

        if (cursor.hasNext()) {
            if (isStudentAlreadyInATeam(teamCollection, request.getStudentID(), request.getCourseID())) 
                throw new WebApplicationException(Response.status(Response.Status.CONFLICT).entity("Student is already in a team.").build());
            if (mode.equals("CREATE")) {
                if (isTeamCreated(teamCollection, request.getTeamID(), request.getCourseID())) 
                    throw new WebApplicationException(Response.status(Response.Status.CONFLICT).entity("Team is already created.").build());
            } else if (mode.equals("JOIN")) {
                if (!isTeamCreated(teamCollection, request.getTeamID(), request.getCourseID())) 
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Team not found.").build());
            }
        }
    }

    /**
     * Checks the passed in params in switchTeam interface
     * @param teamCollection
     * @param courseDocument
     * @param request
     */
    public void switchTeamSecurity(MongoCollection<Document> teamCollection, Document courseDocument, SwitchTeamParam request) {
        MongoCursor<Document> cursor = teamCollection.find().iterator();
        if (cursor == null) 
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve team collection.").build());

        if (!isStudentValid(courseDocument, request.getStudentID())) 
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Student not found in this course.").build());

        if (cursor.hasNext()) {
            if (!isStudentAlreadyInATeam(teamCollection, request.getStudentID(), request.getCourseID())) 
                throw new WebApplicationException(Response.status(Response.Status.CONFLICT).entity("Student not in any team.").build());
            if (!isTeamCreated(teamCollection, request.getTargetTeamID(), request.getCourseID())) 
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Target team not found.").build());
            if (!isTeamCreated(teamCollection, request.getCurrentTeamID(), request.getCourseID()))
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Current team not found.").build());
        }
        if (!isStudentInThisTeam(teamCollection, request.getCurrentTeamID(), request.getStudentID(), request.getCourseID() ))
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Student not found in current team.").build());
        if (isStudentInThisTeam(teamCollection, request.getTargetTeamID(), request.getStudentID(), request.getCourseID() ))
            throw new WebApplicationException(Response.status(Response.Status.CONFLICT).entity("Student already in target team.").build());
        if (isTeamFull(teamCollection, request.getTargetTeamID(), request.getCourseID()))
            throw new WebApplicationException(Response.status(Response.Status.CONFLICT).entity("Target team already full.").build());
        }

}
