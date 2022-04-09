import { useEffect } from "react";
import {useDispatch, useSelector} from "react-redux";
import "./styles/StudentAss.css"
import {Link, useParams} from "react-router-dom";
import { getCourseAssignmentsAsync, setCurrentAssignment } from "../redux/features/assignmentSlice";

const ToDoComponent = () => {
    const dispatch = useDispatch()
    const store = useSelector((state) => state)
    const { role } = store.auth
    const { courseAssignments } = store.assignments
    const { courseId } = useParams()

    useEffect(() => {
        dispatch(getCourseAssignmentsAsync(courseId))
    },[])

    const assignmentClickHandler = (assignment) => {
        dispatch(setCurrentAssignment(assignment))
    }

    return (
        <h3>
            <div id="assList">
                {courseAssignments.map(assignment =>
                    <Link to={`/details/${role}/${courseId}/${assignment.assignment_id}`}
                          onClick={() => assignmentClickHandler(assignment)}>
                        <li id="assListItem">{assignment.assignment_name + "\n\n" + "Due Date: " + assignment.due_date}</li>
                    </Link>
                )}
            </div>
        </h3>

    )
}

export default ToDoComponent
