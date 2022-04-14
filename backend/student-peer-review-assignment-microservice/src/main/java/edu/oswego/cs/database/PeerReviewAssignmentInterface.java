package edu.oswego.cs.database;

import com.ibm.websphere.jaxrs20.multipart.IAttachment;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import edu.oswego.cs.daos.FileDAO;
import org.bson.Document;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class PeerReviewAssignmentInterface {
    private final MongoCollection<Document> studentCollection;
    private final MongoCollection<Document> courseCollection;
    private final MongoCollection<Document> teamCollection;
    private final MongoCollection<Document> assignmentCollection;
    private final MongoCollection<Document> submissionsCollection;
    private final String reg = "/";

    public PeerReviewAssignmentInterface() {
        DatabaseManager databaseManager = new DatabaseManager();
        try {
            MongoDatabase studentDB = databaseManager.getStudentDB();
            MongoDatabase courseDB = databaseManager.getCourseDB();
            MongoDatabase teamDB = databaseManager.getTeamDB();
            MongoDatabase assignmentDB = databaseManager.getAssignmentDB();
            studentCollection = studentDB.getCollection("students");
            courseCollection = courseDB.getCollection("courses");
            teamCollection = teamDB.getCollection("teams");
            assignmentCollection = assignmentDB.getCollection("assignments");
            submissionsCollection = assignmentDB.getCollection("submissions");
        } catch (WebApplicationException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Failed to retrieve collections.").build());
        }
    }

    public List<String> getCourseStudentIDs(String courseID) {
        Document courseDocument = courseCollection.find(eq("course_id", courseID)).first();
        return (List<String>) courseDocument.get("students");
    }

    public boolean makeFileStructure(Set<String> teams, String courseID, int assignmentID) {
        String path = System.getProperty("user.dir") + reg + courseID + reg + assignmentID;
        ArrayList<String> validNames = new ArrayList<>();
        validNames.add("peer-reviews");
        validNames.add("assignments");
        validNames.add("team-submissions");
        validNames.add("peer-review-submissions");
        File[] structure = new File(path).listFiles();
        for (File f : structure) {
            if (validNames.contains(f.getName())) {
                validNames.remove(f.getName());
            } else return false;
        }
        if (validNames.size() != 0) return false;
        if (Objects.requireNonNull(new File(path + reg + "peer-review-submissions").listFiles()).length != 0)
            return false;
        path += reg + "peer-review-submissions";
        for (String team : teams) {
            new File(path + reg + team).mkdir();
        }
        return true;
    }

    public void uploadPeerReview(String courseID, int assignmentID, String srcTeamName, String destTeamName, IAttachment attachment) throws IOException {
        String basePath = FileDAO.peer_review_submission_path + courseID + "/" + assignmentID + "/";
        if (!new File(basePath).exists()) {
            new File(basePath).mkdirs();
        }

        FileDAO fileDAO = FileDAO.fileFactory(courseID, srcTeamName, destTeamName, assignmentID, attachment);

        OutputStream outputStream = new FileOutputStream(basePath + fileDAO.fileName + ".pdf");
        outputStream.write(fileDAO.inputStream.readAllBytes());
        outputStream.close();

    }

    public String packagePeerReviewedAssignments(String courseID, int assignmentID, String teamName) {
        if (!new File(FileDAO.peer_review_submission_path).exists())
            throw new WebApplicationException("Peer reviews do not exist for this course yet.");

        String dir = FileDAO.peer_review_submission_path + courseID + "/" + assignmentID + "/";
        List<File> files = Arrays.asList(new File(dir).listFiles());

        List<File> teamPeerReviews = files.stream().filter(f -> f.getName().split(".pdf")[0].endsWith(teamName)).collect(Collectors.toList());

        String zipPath = "peer-review-submissions/" + courseID + "/" + assignmentID + "/" + "for-" + teamName.concat(".zip");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(zipPath);
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
            for (File f : teamPeerReviews) {
                zipOutputStream.putNextEntry(new ZipEntry("for-" + teamName + "/" + f.getName()));
                byte[] fileBytes = Files.readAllBytes(Paths.get(dir + f.getName()));
                zipOutputStream.write(fileBytes, 0, fileBytes.length);
                zipOutputStream.closeEntry();
            }
            zipOutputStream.close();
            return zipPath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public List<Document> getUsersGradedAssignments(String courseID, int assignmentID, String studentID){
        MongoCursor<Document> query = submissionsCollection.find(and(eq("course_id",courseID),
                                                                    eq("assignment_id",assignmentID),
                                                                    eq("members",studentID),
                                                                    eq("type","peer_review"))).iterator();
        List<Document> assignments = new ArrayList<>();
        while (query.hasNext()) {
            Document document = query.next();
            assignments.add(document);
        }
        if (assignments.isEmpty())
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Assignment does not exist").build());

        assignments.forEach(assignment -> assignment.remove("_id"));
        return assignments;
    }

    public List<Document> getAssignmentsReviewedByUser(String courseID, int assignmentID, String studentID){
        MongoCursor<Document> query = submissionsCollection.find(and(eq("course_id",courseID),
                eq("assignment_id",assignmentID),
                eq("members",studentID),
                eq("type","peer_review_by_me"))).iterator();
        List<Document> assignments = new ArrayList<>();
        while (query.hasNext()) {
            Document document = query.next();
            assignments.add(document);
        }
        if (assignments.isEmpty())
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Assignment does not exist").build());

        assignments.forEach(assignment -> assignment.remove("_id"));
        return assignments;
    }

    public List<Document> getAllUserAssignments(String courseID, int assignmentID, String studentID){
        MongoCursor<Document> query = submissionsCollection.find(and(eq("course_id",courseID),
                eq("assignment_id",assignmentID),
                eq("members",studentID),
                eq("type","team_submission"))).iterator();
        List<Document> assignments = new ArrayList<>();
        while (query.hasNext()) {
            Document document = query.next();
            assignments.add(document);
        }
        if (assignments.isEmpty())
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Assignment does not exist").build());

        assignments.forEach(assignment -> assignment.remove("_id"));
        return assignments;
    }

    public List<String> getCourseTeams(String courseID) {
        ArrayList<String> teamNames = new ArrayList<>();
        for (Document teamDocument : teamCollection.find(eq("course_id", courseID))) {
            String teamName = (String) teamDocument.get("team_id");
            teamNames.add(teamName);
        }
        return teamNames;
    }

    public Document addAssignedTeams(Map<String, List<String>> peerReviewAssignments, String courseID, int assignmentID) {

        for (Document assignmentDocument : assignmentCollection.find(eq("course_id", courseID))) {
            if ((int) assignmentDocument.get("assignment_id") == assignmentID) {

                Document doc = new Document();
                for (String team : peerReviewAssignments.keySet()) {
                    doc.put(team, peerReviewAssignments.get(team));
                }
                makeFileStructure(peerReviewAssignments.keySet(), courseID, assignmentID);
                assignmentCollection.updateOne(assignmentDocument, set("assigned_teams", doc));
                return doc;
            }
        }
        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Failed to add assigned teams.").build());
    }

    public Document getAssignmentDocument(String courseID, int assignmentID) {
        for (Document assignmentDocument : assignmentCollection.find(eq("course_id", courseID))) {
            if ((int) assignmentDocument.get("assignment_id") == assignmentID) {
                return assignmentDocument;
            }
        }
        throw new WebApplicationException("No course/assignmentID found.");
    }
}