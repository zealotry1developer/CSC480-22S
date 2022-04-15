package edu.oswego.cs.resources;

import edu.oswego.cs.database.TeamInterface;
import edu.oswego.cs.requests.TeamParam;

import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/teams/team")
@DenyAll
public class StudentTeamResources {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("create")
    @RolesAllowed({"professor","student"})
    public Response createTeam(TeamParam request) {
        new TeamInterface().createTeam(request);
        return Response.status(Response.Status.CREATED).entity("Team successfully created.").build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get/unlocked-team/all")
    @RolesAllowed("student")
    public Response getTeamByStudentID(TeamParam request) {
        return Response.status(Response.Status.OK).entity(new TeamInterface().getAllUnlockedTeamByStudentID(request)).build();
    }

    @GET
    @Path("get/team_id")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"professor","student"})
    public Response getTeamByTeamID(TeamParam request) {
        return Response.status(Response.Status.OK).entity(new TeamInterface().getTeamByTeamID(request)).build();
    }
    
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("join")
    @RolesAllowed("student")
    public Response joinTeam(TeamParam request) {
        new TeamInterface().studentJoinTeam(request);
        return Response.status(Response.Status.OK).entity("Student successfully join team.").build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("team-lead/step-down")
    @RolesAllowed("student")
    public Response giveUpTeamLead(TeamParam request) {
        new TeamInterface().giveUpTeamLead(request);
        return Response.status(Response.Status.OK).entity("Team Lead successfully updated.").build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("team-lead/cede")
    @RolesAllowed("student")
    public Response nominateTeamLead(TeamParam request) {
        new TeamInterface().nominateTeamLead(request);
        return Response.status(Response.Status.OK).entity("Team Lead successfully updated.").build();
    }
    
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("generate-team-name")
    @RolesAllowed("student")
    public Response generateTeamName(TeamParam request) {
        new TeamInterface().generateTeamName(request);
        return Response.status(Response.Status.OK).entity("Team name successfully generated").build(); 
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("leave")
    @RolesAllowed("student")
    public Response leaveTeam(TeamParam request) {
        new TeamInterface().leaveTeam(request);
        return Response.status(Response.Status.OK).entity("Student successfully leave team.").build();
    }


}




