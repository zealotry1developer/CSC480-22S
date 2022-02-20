package edu.oswego.cs.daos;

import lombok.*;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@NoArgsConstructor
@Getter
public class CourseDAO {

    @Id
    public @JsonbProperty("courseName") @NonNull String courseName;
    public @JsonbProperty("courseSection") @NonNull int courseSection;
    public String professor;

    @JsonbCreator
    public CourseDAO(@JsonbProperty("courseName") String courseName,
                     @JsonbProperty("courseSection") int courseSection) {
        this.courseName = courseName;
        this.courseSection = courseSection;
    }

    public String toString() {
        return String.format("{\"courseName\":\"%s\",\"courseSection\":%d}", courseName, courseSection);
    }
}
