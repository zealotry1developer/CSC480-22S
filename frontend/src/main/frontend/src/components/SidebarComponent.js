//import useState hook to create menu collapse state
import React  from 'react';
import {Link, useNavigate} from "react-router-dom";

//import react pro sidebar components
import {
  ProSidebar,
  Menu,
  MenuItem,
  SidebarFooter,
  SidebarContent,
} from 'react-pro-sidebar';

//import icons from react icons
import { FaList } from 'react-icons/fa';
import {
  FiLogOut,
} from 'react-icons/fi';

//import sidebar css from react-pro-sidebar module and our custom css
import 'react-pro-sidebar/dist/css/styles.css';
import './styles/Sidebar.css';

const SidebarComponent = () => {
    let navigate = useNavigate()

    const handleLogout = () => {
        localStorage.clear();
        navigate('/')
        window.location.reload(false);
    }

    return (
    <>
      <div id='sidebar'>
        <ProSidebar>
          <SidebarContent>
           {/* Need to use jwt to decide which dashboard to goto*/}
           <Link to="/teacherDashboard">
            <Menu iconShape='circle'>
              <MenuItem icon={<FaList />}>Courses</MenuItem>
            </Menu>
           </Link>
          </SidebarContent>
          <SidebarFooter>
          <Menu iconShape='circle'>
            <MenuItem icon={<FiLogOut />} onClick={handleLogout}>Logout</MenuItem>
          </Menu>
          </SidebarFooter>
        </ProSidebar>
      </div>
    </>
    );
};

export default SidebarComponent;
