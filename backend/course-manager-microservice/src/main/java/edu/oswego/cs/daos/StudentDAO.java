package edu.oswego.cs.daos;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@NoArgsConstructor
public class StudentDAO {
    @Id
    @JsonbProperty("email") public String email;
    @JsonbProperty("abbreviation") public String abbreviation;
    @JsonbProperty("course_name") public String courseName;
    @JsonbProperty("course_section") public String courseSection;
    @JsonbProperty("semester") public String semester;
    @JsonbProperty("year") public String year;
    @JsonbProperty("crn") public int crn;
    public String fullName;

    public StudentDAO(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }

    @JsonbCreator
    public StudentDAO(
            @NonNull @JsonbProperty("email") String email,
            @NonNull @JsonbProperty("abbreviation") String abbreviation,
            @NonNull @JsonbProperty("course_name") String courseName,
            @NonNull @JsonbProperty("course_section") String courseSection,
            @NonNull @JsonbProperty("semester") String semester,
            @NonNull @JsonbProperty("year") String year,
            @NonNull @JsonbProperty("crn") int crn) {
        this.email = email;
        this.abbreviation = abbreviation;
        this.courseName = courseName;
        this.courseSection = courseSection;
        this.semester = semester;
        this.year = year;
        this.crn = crn;
    }

    public String toString() {
        return this.email + "/" + this.fullName;
    }
}