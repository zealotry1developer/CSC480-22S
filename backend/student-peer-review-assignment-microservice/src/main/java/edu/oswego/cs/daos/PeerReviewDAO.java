package edu.oswego.cs.daos;

import javax.json.bind.annotation.JsonbProperty;
import javax.persistence.Id;

public class PeerReviewDAO {

    @Id @JsonbProperty("grade") public int grade;

}
