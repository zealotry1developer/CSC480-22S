package edu.oswego.cs.database;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;

import edu.oswego.cs.daos.TeamDAO;
import edu.oswego.cs.requests.SwitchTeamParam;
import edu.oswego.cs.requests.TeamParam;
import edu.oswego.cs.services.SecurityService;
import edu.oswego.cs.services.TeamService;

import org.bson.Document;
import org.bson.conversions.Bson;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class TeamInterface {
    private final MongoCollection<Document> courseCollection;
    private final MongoCollection<Document> studentCollection;
    private final MongoCollection<Document> teamCollection;

    /**
     * Default constructor initializes database connections and retrieves needed collections 
     */
    public TeamInterface() {
        DatabaseManager databaseManager = new DatabaseManager();
        try {
            MongoDatabase studentDB = databaseManager.getStudentDB();
            MongoDatabase courseDB = databaseManager.getCourseDB();
            MongoDatabase teamDB = databaseManager.getTeamDB();
            studentCollection = studentDB.getCollection("students");
            courseCollection = courseDB.getCollection("courses");
            teamCollection = teamDB.getCollection("teams");
        } catch (WebApplicationException e) {
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve collections.").build());
        }
    }

    /**
     * Allows user to create and write a team to TeamDatabase
     * @param request TeamDAO:{"course_id", "team_lead", "team_size"}
     */
    public void createTeam(TeamParam request) {
        /* Course Security Checks */
        Document courseDocument = courseCollection.find(eq("course_id", request.getCourseID())).first();
        if (courseDocument == null) 
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Course not found.").build());
        
        /* Param Security Checks */
        new SecurityService().createTeamSecurity(teamCollection, courseDocument, request);
            
        /* Create Team */
        String teamID = new TeamService().generateTeamID(teamCollection);
        int teamSize = new TeamService().getTeamSize(courseDocument, request);
        TeamDAO newTeam = new TeamDAO(teamID, request.getCourseID(), teamSize, request.getStudentID() );
        newTeam.getTeamMembers().add(request.getStudentID());
        newTeam.setTeamMembers(newTeam.getTeamMembers());

        /* Write to DB */
        Jsonb jsonb = JsonbBuilder.create();
        Entity<String> courseDAOEntity = Entity.entity(jsonb.toJson(newTeam), MediaType.APPLICATION_JSON_TYPE);
        Document teamDocument = Document.parse(courseDAOEntity.getEntity());
        teamCollection.insertOne(teamDocument);
    }

    /**
     * Gets all teams in a course
     * @param request TeamParam:{"course_id"}
     * @return List<Document> of all team
     */
    public List<Document> getAllTeams(TeamParam request) {
        /* Course Security Checks */
        Document courseDocument = courseCollection.find(eq("course_id", request.getCourseID())).first();
        if (courseDocument == null) 
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Course not found.").build());
        
        /* Get Team */
        MongoCursor<Document> cursor = teamCollection.find().iterator();
        if (cursor == null)
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve team collection.").build());
        
        List<Document> teams = new ArrayList<>();

        try { 
            while(cursor.hasNext()) { 
                Document teamDocument = cursor.next();
                if (teamDocument.get("course_id").toString().equals(request.getCourseID())) 
                    teams.add(teamDocument);     
            } 
        } finally { 
            cursor.close(); 
        } 
        return teams;
    }

    /**
     * If requested student is not already in a team, shows non-full-teams
     * If requested student is already in a team, shows their team
     * @param request TeamParam:{"course_id", "student_id"}
     * @return List<Document> of teams
     */
    public List<Document> getTeamByStudentID(TeamParam request) {
        /* Course Security check */
        Document courseDocument = courseCollection.find(eq("course_id", request.getCourseID())).first();
        if (courseDocument == null) 
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Course not found.").build());
        
        /* Param Security Checks */
        if (!new SecurityService().isStudentValid(courseDocument, request.getStudentID())) 
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Student not found in this course.").build());

        /* Get Team */
        List<Document> nonFullTeams = new ArrayList<>();
        MongoCursor<Document> cursor = teamCollection.find().iterator();
        if (cursor == null) 
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve team collection.").build());

        try { 
            while(cursor.hasNext()) { 
                Document teamDocument = cursor.next();
                List<String> members = teamDocument.getList("team_members", String.class);
                String courseID = teamDocument.get("course_id").toString();
                
                for (String member : members) {
                    List<Document> teams = new ArrayList<>();
                    if (request.getStudentID().equals(member) && request.getCourseID().equals(courseID)){
                        teams.add(teamDocument);
                        return teams; 
                    }
                }
                if (!teamDocument.getBoolean("is_full") && request.getCourseID().equals(courseID)) 
                    nonFullTeams.add(teamDocument);
            }
        } finally { 
            cursor.close();
        } 
        return nonFullTeams;
    }

    /**
     * Gets team by teamID
     * @param request TeamParam:{"course_id", "team_id"}
     * @return Document
     */
    public Document getTeamByTeamID(TeamParam request) {
        /* Course Security check */
        Document courseDocument = courseCollection.find(eq("course_id", request.getCourseID())).first();
        if (courseDocument == null) 
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Course not found.").build());
        
        /* Get Team */
        MongoCursor<Document> cursor = teamCollection.find().iterator();
        if (cursor == null) 
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve team collection.").build());
        
        try { 
            while(cursor.hasNext()) { 
                Document teamDocument = cursor.next();
                String courseID = teamDocument.get("course_id").toString();
                String teamID = teamDocument.get("team_id").toString();
                
                if (request.getTeamID().equals(teamID) && request.getCourseID().equals(courseID))
                    return teamDocument;
            }
        } finally { 
            cursor.close();
        } 

        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Team not found.").build());
    }

    /**
     * Allows student user to join a team
     * @param request TeamDAO:{"team_id", "course_id", "team_lead"}
     */
    public void joinTeam(TeamParam request) {
        /* Course Security check */
        Document courseDocument = courseCollection.find(eq("course_id", request.getCourseID())).first();
        if (courseDocument == null) 
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Course not found.").build());
        
        /* Param Security Checks */
        new SecurityService().joinTeamSecurity(teamCollection, courseDocument, request);

        /* Update Team */
        Document teamDocument = teamCollection.find(eq("team_id", request.getTeamID())).first();
        List<String> teamMembers = teamDocument.getList("team_members", String.class);
        teamMembers.add(request.getStudentID());
        teamCollection.updateOne(Filters.eq("team_id", request.getTeamID()), Updates.set("team_members", teamMembers));

        if (teamMembers.size() == teamDocument.getInteger("team_size")) 
            teamCollection.updateOne(Filters.eq("team_id", request.getTeamID()), Updates.set("is_full", true));
    }

    /**
     * Allows student users to switch teams
     * @param request SwitchTeamParam:{"course_id", "student_id", "current_team_id", "target_team_id"}
     */
    public void switchTeam(SwitchTeamParam request) {
        /* Course Security check */
        Document courseDocument = courseCollection.find(eq("course_id", request.getCourseID())).first();
        if (courseDocument == null) 
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Course not found.").build());

        /* Param Security Checks */
        new SecurityService().switchTeamSecurity(teamCollection, courseDocument, request);

        /* Update currentTeamDocument */
        Document currentTeamDocument = teamCollection.find(eq("team_id", request.getCurrentTeamID())).first();
        List<String> currentTeamMembers = currentTeamDocument.getList("team_members", String.class);
        currentTeamMembers.remove(request.getStudentID());

        if (currentTeamMembers.size() < 1) 
            teamCollection.deleteOne(eq("team_id", request.getCurrentTeamID()));
        else {
            Bson currentTeamUpdates = Updates.combine(
                Updates.set("team_members", currentTeamMembers), 
                Updates.set("team_lead", currentTeamMembers.get(0)), 
                Updates.set("is_full", false));
            UpdateOptions currentTeamOptions = new UpdateOptions().upsert(true);
    
            try {
                teamCollection.updateOne(new Document("team_id", request.getCurrentTeamID()), currentTeamUpdates, currentTeamOptions);
            } catch (MongoException error){
                error.printStackTrace();
            }
        }
        
        /* Update targetTeamDocument  */
        Document targetTeamDocument = teamCollection.find(eq("team_id", request.getTargetTeamID())).first();
        List<String> targetTeamMembers = targetTeamDocument.getList("team_members", String.class);
        targetTeamMembers.add(request.getStudentID());

        Bson targetTeamUpdates = Updates.combine(
            Updates.set("team_members", targetTeamMembers),
            Updates.set("is_full", false));

        if (targetTeamMembers.size() == targetTeamDocument.getInteger("team_size")) 
            targetTeamUpdates = Updates.combine(targetTeamUpdates, Updates.set("is_full", true));
        
        UpdateOptions targetTeamOptions = new UpdateOptions().upsert(true);

        try {
            teamCollection.updateOne(new Document("team_id", request.getTargetTeamID()), targetTeamUpdates, targetTeamOptions);
        } catch (MongoException error){
            error.printStackTrace();
        }
    }

    /**
     * Generates a new name for the team
     * @param request TeamParam:{"team_id", "course_id", "student_id", "team_name"}
     */
    public void generateTeamName(TeamParam request) {
        /* Course Security check */
        Document courseDocument = courseCollection.find(eq("course_id", request.getCourseID())).first();
        if (courseDocument == null) 
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Course not found.").build());

        /* Param Security Checks */
        new SecurityService().generateTeamNameSecurity(teamCollection, studentCollection, courseDocument, request);

        /* Update team name */
        Bson teamNameUpdates = Updates.combine(
            Updates.set("team_id", request.getTeamName()),
            Updates.set("team_lock", true)
        );
        UpdateOptions teamNameOptions = new UpdateOptions().upsert(true);

        try {
            teamCollection.updateOne(new Document("team_id", request.getTeamID()), teamNameUpdates, teamNameOptions);
        } catch (MongoException error){
            error.printStackTrace();
        }
    }


    /* ---- Professor ---- */

    /**
     * Deketes a team
     * Toggles the team status
     * @param request TeamParam:{"team_id", "course_id"}
     */
    public void toggleTeamLock(TeamParam request) {
        /* Course Security check */
        Document courseDocument = courseCollection.find(eq("course_id", request.getCourseID())).first();
        if (courseDocument == null)
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Course not found.").build());
        
        /* Param Security Checks */
        if (!new SecurityService().isTeamCreated(teamCollection, request.getTeamID(), request.getCourseID())) 
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Team not found.").build());
        
        /* Update team status */
        Document teamDocument = teamCollection.find(eq("team_id", request.getTeamID())).first();
        boolean teamLock = teamDocument.getBoolean("team_lock");
        teamCollection.findOneAndUpdate(Filters.eq("team_id", request.getTeamID()), Updates.set("team_lock", !teamLock));
    }
    /**
     * Edits a Team Name
     * @param request TeamParam: {"team_id", "course_id", "team_name"}
     */
    public void editTeamName(TeamParam request) {
        /* Course Security check */
        Document courseDocument = courseCollection.find(eq("course_id", request.getCourseID())).first();
        if (courseDocument == null)
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Course not found.").build());
        
        /* Param Security Checks */
        new SecurityService().editTeamNameSecurity(teamCollection, studentCollection, courseDocument, request);
        
        /* Update Team name */
        Bson editTeamNameUpdates = Updates.combine(
            Updates.set("team_id", request.getTeamName()),
            Updates.set("team_lock", true)
        );
        teamCollection.findOneAndUpdate(Filters.eq("team_id", request.getTeamID()), editTeamNameUpdates); 
    }

    /**
     * Assigns a new Team lead for the team
     * @param request
     */
    public void assignTeamLead(TeamParam request) {
         /* Course Security check */
         Document courseDocument = courseCollection.find(eq("course_id", request.getCourseID())).first();
         if (courseDocument == null)
             throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Course not found.").build());
         
         /* Param Security Checks */
         new SecurityService().assignTeamLeadSecurity(teamCollection, courseDocument, request);

         /* Update Team Lead */
         Document teamDocument = teamCollection.find(eq("team_id", request.getTeamID())).first();
         List<String> students = teamDocument.getList("team_members", String.class);
         students.remove(request.getStudentID());
         Collections.reverse(students);
         students.add(request.getStudentID());
         Collections.reverse(students);

         Bson assignTeamLeadUpdates = Updates.combine(
            Updates.set("team_lead", request.getStudentID()),
            Updates.set("team_members", students)
         );

         teamCollection.findOneAndUpdate(Filters.eq("team_id", request.getTeamID()), assignTeamLeadUpdates);
    }

    /**
     * Deletes a team
     * @param request TeamParam:{"team_id", "course_id"}
     */
    public void deleteTeam(TeamParam request) {
        /* Course Security check */
        Document courseDocument = courseCollection.find(eq("course_id", request.getCourseID())).first();
        if (courseDocument == null)
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Course not found.").build());
        
        /* Param Security Checks */
        if (!new SecurityService().isTeamCreated(teamCollection, request.getTeamID(), request.getCourseID()))
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Team not found.").build());
        if (new SecurityService().isTeamLock(teamCollection, request.getTeamID(), request.getCourseID()))
            throw new WebApplicationException(Response.status(Response.Status.NOT_ACCEPTABLE).entity("Team is locked.").build());

        /* Delete team*/
        teamCollection.findOneAndDelete(eq("team_id", request.getTeamID()));
    }

}