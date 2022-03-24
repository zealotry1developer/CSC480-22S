import React from "react-dom";
import {useState} from "react";
import "./styles/EditCourse.css"
import "./styles/DeleteModal.css"
import {useSelector} from "react-redux";
import {useNavigate} from "react-router-dom";
import axios from "axios";

const deleteUrl = `${process.env.REACT_APP_URL}/manage/professor/courses/course/delete`
const editUrl = ""
const csvUrl = ""

const EditCourseComponent = () => {
    let navigate = useNavigate()
    const currentCourse = useSelector((state) => state.courses.currentCourse)

    const [formData, setFormData] = useState({
        course_name: currentCourse.course_name,
        course_csv: null,
        team_size: null,
    });
    const [showModal, setShow] = useState(false)

    const { course_name, team_size } = formData;

    const OnChange = (e) => setFormData(
        { ...formData, [e.target.name]: e.target.value }
    );

    const fileChangeHandler = (event) => {
        let file = event.target.files[0];
        const renamedFile = new File([file], currentCourse.course_id, { type: file.type })
        const fileFormData = new FormData()
        fileFormData.append("File", renamedFile)
        setFormData({course_csv: fileFormData})
    }

    const editCourse = () => {
        // await axios.post(editUrl, currentCourse).then((response) =>{
        //     console.log(response)
        // })
        console.log({...formData})
    }

    const deleteCourse = async () => {
        await axios.post(deleteUrl, currentCourse).then((response) => {
            console.log(response)
        })
        navigate("/")
    }

    const Modal = () => {
        return (
            <div id="deleteModal">
                <div id="modalContent">
                    <span id="deleteSpan">Are you sure you want to delete this course?</span>
                    <div id="deleteButtons">
                        <button onClick={deleteCourse}>Yes</button>
                        <button onClick={() => setShow(false)}>No</button>
                    </div>
                </div>
            </div>
        )
    }

    return (
        <form id="editCourseForm">
            <div className="course-name">
                <label> <b> Name of course: </b> </label>
                <input
                    type="text"
                    name="course_name"
                    value={course_name}
                    required
                    onChange={(e) => OnChange(e)}
                />
            </div>

            <div className="course-csv">
                <label> <b> Add course CSV: </b> </label>
                <input
                    onChange={fileChangeHandler}
                    type="file"
                    name="course_csv"
                    required
                />
            </div>

            <div className="team-size">
                <label> <b> Select peer review team size: </b> </label>
                <input
                    type="number"
                    min="1"
                    name="team_size"
                    required
                    value={team_size}
                    onChange={(e) => OnChange(e)}
                />
            </div>

            <div className="editCourseButton">
                <button onClick={editCourse}> Submit</button>
                <a onClick={() => setShow(true)} target="_blank"> Delete course </a>
                <div>
                    {showModal ? Modal() : null}
                </div>
            </div>
        </form>
    )
}

export default EditCourseComponent