package edu.oswego.cs.requests;
import javax.json.bind.annotation.JsonbProperty;

import com.mongodb.lang.NonNull;

import lombok.Data;

@Data
public class TeamParam {
    @JsonbProperty("course_id") @NonNull String courseID;
    @JsonbProperty("nominated_team_lead") @NonNull String nominatedTeamLead;
    @JsonbProperty("team_id") @NonNull private String teamID;
    @JsonbProperty("team_name") @NonNull private String teamName;
    @JsonbProperty("team_size") @NonNull private Integer teamSize;
    @JsonbProperty("student_id") @NonNull private String studentID;
}
