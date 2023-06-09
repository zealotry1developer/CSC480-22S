import { useEffect, useState } from 'react';
import './styles/AssBar.css';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  getAssignmentDetailsAsync,
  getCombinedAssignmentPeerReviews,
} from '../redux/features/assignmentSlice';
import uuid from 'react-uuid';
import LogoutButton from "./GlobalComponents/LogoutButton";

const AssBarLink = ({ active, assignment, onClick }) => {
  const { role } = useSelector((state) => state.auth);
  const normalStyle = { backgroundColor: '#f6f6f6' };
  const clickedStyle = { backgroundColor: 'white' };
  const { courseId } = useParams();
  const link = `/${role}/${courseId}`;

  return (
    <Link
      to={
        assignment.assignment_type === 'peer-review'
          ? `${link}/${assignment.assignment_id}/peer-review/${assignment.peer_review_team}`
          : `${link}/${assignment.assignment_id}/normal`
      }
      onClick={onClick}
    >
      <tr id='ass-bar'>
        <td style={active ? clickedStyle : normalStyle}>
          <div className='colorForTable' />
          <p className='inter-24-bold courseText'> {assignment.assignment_name} </p>
        </td>
      </tr>
    </Link>
  );
};

const AssBarComponent = () => {
  const dispatch = useDispatch();
  const { combinedAssignmentPeerReviews } = useSelector(
    (state) => state.assignments
  );
  const { courseId, assignmentId, assignmentType, teamId } = useParams();
  const { currentTeamId } = useSelector((state) => state.teams);
  const { lakerId } = useSelector((state) => state.auth);
  const navigate = useNavigate();

  const curr =
    assignmentType === 'peer-review'
      ? `${assignmentId}-peer-review-${teamId}`
      : parseInt(assignmentId);
  const [chosen, setChosen] = useState(curr);

  useEffect(() => {
    dispatch(
      getCombinedAssignmentPeerReviews({ courseId, currentTeamId, lakerId })
    );
  }, [courseId, currentTeamId, lakerId, dispatch]);

  const onAssClick = (assignment) => {
    const curr =
      assignment.assignment_type === 'peer-review'
        ? `${assignment.assignment_id}-${assignment.assignment_type}-${assignment.peer_review_team}`
        : parseInt(assignment.assignment_id);
    setChosen(curr);
    const courseId = assignment.course_id;
    const assignmentId = assignment.assignment_id;
    dispatch(getAssignmentDetailsAsync({ courseId, assignmentId }));
  };

  // const onCourseClick = () => {
  //   navigate(`/student/${courseId}`);
  // };

  return (
    <div className='abc-parent'>
      <div className='abc-title'>
        <h2 className='inter-44-bold'> Assignments </h2>
      </div>
      <div className='abc-assignments'>
        {combinedAssignmentPeerReviews.map(
          (assignment) =>
            assignment && (
              <AssBarLink
                key={uuid()}
                onClick={() => onAssClick(assignment)}
                active={assignment.final_id === chosen}
                assignment={assignment}
              />
            )
        )}
      </div>
      <LogoutButton/>
    </div>
  );
};

export default AssBarComponent;
