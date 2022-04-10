import { Route, Routes } from 'react-router-dom';
import App from '../App';
import CreateCoursePage from '../pages/TeacherPages/CreateCoursePage';
import ProfessorCoursePage from '../pages/TeacherPages/ProfessorCoursePage';
import StudentCoursePage from "../pages/StudentPages/StudentCoursePage";
import CreateAssignmentPage  from "../pages/TeacherPages/CreateAssignmentPage";
import AssignmentPage from "../pages/StudentPages/AssignmentPage";
import UnauthedErrorPage from '../pages/AuthPages/UnauthedErrorPage';
import React from 'react';
import RoleRouteHandler from "./RoleRouteHandler";
import AuthRouteHandler from "./AuthRouteHandler";
import ProfessorAssignmentView from "../pages/TeacherPages/ProfessorAssignmentView";
import axiosInterceptor from "./interceptors/axiosInterceptor";

const RouterHandler = () => {

    axiosInterceptor()

    return (
        <Routes>
          <Route>
              {/*public routes*/}
              <Route path="unauthenticated" element={<UnauthedErrorPage />} />
              <Route path="*" element={<p> Page doesn't exist </p>} />

              <Route element={<AuthRouteHandler/>}>
                  <Route path="/" element={<App />} />

                  {/*professor-only routes*/}
                  <Route element={<RoleRouteHandler allowedRoles={["professor"]}/>} >
                      <Route path="create/course" element={<CreateCoursePage/>}/>
                      <Route path='details/professor/:courseId' element={<ProfessorCoursePage />} />
                      <Route path='details/professor/:courseId/create/assignment' element={<CreateAssignmentPage />} />
                      <Route path='details/professor/:courseId/:assignmentId/grade' element={<ProfessorAssignmentView />} />
                  </Route>

                  {/*student routes*/}
                  <Route element={<RoleRouteHandler allowedRoles={["student", "professor"]}/>} >
                      <Route path='details/student/:courseId' element={<StudentCoursePage />} />
                      <Route path='details/student/:courseId/:assignmentId' element={<AssignmentPage />}/>
                  </Route>
              </Route>
          </Route>
        </Routes>
    );
};

export default RouterHandler;
