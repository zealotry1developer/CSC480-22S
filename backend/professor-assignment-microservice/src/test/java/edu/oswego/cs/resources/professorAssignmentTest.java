package edu.oswego.cs.resources;

import edu.oswego.cs.rest.daos.FileDAO;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class professorAssignmentTest {
    private static String port;
    private static String baseUrl;
    private static String targetUrl;
    private Client client;
    private static ArrayList<File> allFiles;
    private static ArrayList<File> expectedFiles;

    @BeforeAll
    public static void oneTimeSetup(){
        port = "13130";
        baseUrl = "http://moxie.cs.oswego.edu:" + port + "assignments/professor";

        String file1Name = "compressed(zipped)Folder.zip";
        File file1 = new File(FileDAO.getProjectRootDir() + "src/test/java/edu/oswego/cs/resources/exampleFiles/" + file1Name);

        String file2Name = "portableDocumentFormat.pdf";
        File file2 = new File(FileDAO.getProjectRootDir() + "src/test/java/edu/oswego/cs/resources/exampleFiles/" + file2Name);

        String file3Name = "portableNetworkGraphics.png";
        File file3 = new File(FileDAO.getProjectRootDir() + "src/test/java/edu/oswego/cs/resources/exampleFiles/" + file3Name);

        String file4Name = "text.txt";
        File file4 = new File(FileDAO.getProjectRootDir() + "src/test/java/edu/oswego/cs/resources/exampleFiles/" + file4Name);

        if (file1.exists() && file2.exists() && file3.exists() && file4.exists())
            System.out.println("THIS FILE FREAKING EXIST");

        allFiles = new ArrayList<>();
        allFiles.add(file1);
        allFiles.add(file2);
        allFiles.add(file3);
        allFiles.add(file4);

        expectedFiles = new ArrayList<>();
        expectedFiles.add(file1);
        expectedFiles.add(file2);
    }

    @BeforeEach
    public void setup() {
        client = ClientBuilder.newClient();
        allFiles.forEach(file -> {
            String uploadURL = baseUrl + "/courses/&&SoftwareDesignI&&/assignments/-1/upload";
            WebTarget target = client.target(uploadURL);
            target.request(MediaType.MULTIPART_FORM_DATA)
                    .accept(MediaType.MULTIPART_FORM_DATA)
                    .post(Entity.entity(file, MediaType.MULTIPART_FORM_DATA));
        });
//        expectedFiles.forEach(file -> {
////            String createURL = "http://moxie.cs.oswego.edu:13127/manage/professor/courses/course/create/";
//            String uploadURL = baseUrl + "/courses/course/assignments/upload";
//            WebTarget target = client.target(uploadURL);
//            target.request(MediaType.MULTIPART_FORM_DATA)
//                    .accept(MediaType.MULTIPART_FORM_DATA)
//                    .post(Entity.entity(file, MediaType.MULTIPART_FORM_DATA));
//        });

    }

    @AfterEach
    public void teardown() throws IOException {
//
//        allFiles.forEach(file -> {
////            String deleteURL = "http://moxie.cs.oswego.edu:13127/manage/professor/courses/course/delete/";
//            String removeURL = baseUrl + "/courses/course/assignments/remove";
//            WebTarget target = client.target(removeURL);
//            target.request(MediaType.MULTIPART_FORM_DATA)
//                    .accept(MediaType.MULTIPART_FORM_DATA)
//                    .post(Entity.entity(file, MediaType.MULTIPART_FORM_DATA));
//        });
        File assignmentFolder = new File(FileDAO.getProjectRootDir() + "/&&SoftwareDesignI&&");
        if (assignmentFolder.exists())
            FileUtils.deleteDirectory(assignmentFolder);
        client.close();
    }

    @Test
    public void ProfessorAssignment_UploadTest() throws FileNotFoundException {

        File file = new File(FileDAO.getProjectRootDir() + "/&&SoftwareDesignI&&");
        File[] files = file.listFiles();
//        Arrays.asList(files).forEach(uploadedFile -> {
//            try {
//                if (files.length != 2 || !(
//                    FileUtils.contentEquals(uploadedFile, expectedFiles.get(0)) ||
//                    FileUtils.contentEquals(uploadedFile, expectedFiles.get(1))
//                    )){
//
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//    });
        Assertions.assertEquals(files.length, 2,
                "The wrong files were allowed/blocked. \nIf > 2: \"allowed\" if < 2: \"blocked\"");
        Assertions.assertEquals(new FileInputStream(files[0]), new FileInputStream(expectedFiles.get(0)),
                "The file uploaded: "+ files[0].getName() +", and the file expected: "+ expectedFiles.get(0) +" are different.");
        Assertions.assertEquals(new FileInputStream(files[1]), new FileInputStream(expectedFiles.get(1)),
                "The file uploaded: "+ files[0].getName() +", and the file expected: "+ expectedFiles.get(0) +" are different.");
    }

    @Test
    public void ProfessorAssignment_deleteTest() {
//        targetUrl = "/courses/&&SoftwareDesignI&&/assignments/{assignmentID}/remove\n";
//        WebTarget target = client.target(baseUrl + targetUrl);
//        deleteAssignmentResponse = target.request(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(jsonb.toJson(assignment), MediaType.APPLICATION_JSON));
//
//        Assertions.assertEquals(Response.Status.OK, Response.Status.fromStatusCode(deleteAssignmentResponse.getStatus()), "Assignment was not deleted properly.");
//        assignmentDeletedTest = true;
        AtomicInteger index = new AtomicInteger(0);
        allFiles.forEach(file -> {
            String deleteURL = baseUrl + "/courses/&&SoftwareDesignI&&/assignments/" + index.getAndIncrement() + "/remove";
            WebTarget target = client.target(deleteURL);
            target.request(MediaType.MULTIPART_FORM_DATA)
                    .accept(MediaType.MULTIPART_FORM_DATA)
                    .delete();
        });

        File file = new File(FileDAO.getProjectRootDir() + "/&&SoftwareDesignI&&");
        File[] files = file.listFiles();

        Assertions.assertEquals(files.length, 0,
                "The files were not deleted.");
    }
}