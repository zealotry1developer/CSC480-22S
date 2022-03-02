import TeacherDashboardPage from "./pages/TeacherDashboardPage";
import LoginPage from "./pages/LoginPage";
import { useSelector } from "react-redux";
import "./global_styles/global.css"

function App() {
    const authentication = useSelector((state) => state);
    const isAuthenticated = authentication.auth.isAuthenticated;

    return (
      <div>
          {isAuthenticated? <TeacherDashboardPage/> : <LoginPage />}
      </div>
  );
}

export default App;
