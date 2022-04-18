import { configureStore } from "@reduxjs/toolkit";
import courseReducer from './features/courseSlice'
import authReducer from "./features/authSlice";
import assignmentReducer from "./features/assignmentSlice";
import peerReviewReducer from "./features/peerReviewSlice";
import teamReducer from "./features/teamSlice";

export default configureStore({
    reducer: {
        courses: courseReducer,
        auth: authReducer,
        assignments: assignmentReducer,
        peerReviews: peerReviewReducer,
        teams: teamReducer
    },
});
