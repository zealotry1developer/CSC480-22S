import React, {useEffect, useState} from 'react';
import {Link} from 'react-router-dom';
import SidebarComponent from '../../components/SidebarComponent';
import './styles/ProfessorDashboardStyle.css';
import Loader from '../../components/LoaderComponenets/Loader';
import {useDispatch, useSelector} from 'react-redux';
import {getCourseDetailsAsync, getCoursesAsync} from '../../redux/features/courseSlice';

function ProfessorDashboardPage() {
    const [isLoading, setIsLoading] = useState(false);
    const dispatch = useDispatch();
    const courses = useSelector((state) => state.courses.courses);
    const user = useSelector((state) => state.auth.user_given_name);

    useEffect(() => {
        setIsLoading(true);
        dispatch(getCoursesAsync());
        setTimeout(() => setIsLoading(false), 750);
    }, []);

    const studentView = () => {
        localStorage.setItem('alt_role', 'student');
        window.location.reload(false);
    };

    const onCourseClick = (course) => {
        dispatch(getCourseDetailsAsync(course.course_id));
    };

    return (
        <div>
            {isLoading ? (
                <Loader/>
            ) : (
                <div className={'TeacherDashboard'}>
                    <SidebarComponent/>
                    <div id='teacher'>
                        <div className='welcome-banner'>
                            <h1 className="kumba-30">Hello, {user}!</h1>
                            <div className='views'>
                                <button className='kumba-25 student-view' onClick={studentView}>
                                    Student View
                                </button>
                            </div>
                        </div>

                        <div id='proCourseList'>
                            {courses.map((course) => (
                                <Link
                                    to={'/details/professor/' + course.course_id}
                                    onClick={() => onCourseClick(course)}>
                                    <li className='courseListItem'>
                                        <span className="outfit-16 pdp-coursename">{course.course_id}</span>
                                        <span className="kumba-25">{course.course_name}</span>
                                    </li>
                                </Link>
                            ))}
                        </div>
                        <div id='addClass'>
                            <Link to='/create/course'>
                                <button className="yellow-button" id='addButton'>Create new course</button>
                            </Link>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default ProfessorDashboardPage;
