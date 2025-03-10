package com.example.touchtyped.firestore;

import java.util.List;

public class Classroom {

    private String teacherID;
    private String classroomID;
    private List<String> studentUsernames;

    public Classroom() {}

    public Classroom(String teacherID, String classroomID) {
        this.teacherID = teacherID;
        this.classroomID = classroomID;
    }

    public Classroom(String teacherID, String classroomID, List<String> studentUsernames) {
        this.teacherID = teacherID;
        this.classroomID = classroomID;
        this.studentUsernames = studentUsernames;
    }

    // GETTERS AND SETTERS

    public String getTeacherID() {
        return teacherID;
    }

    public void setTeacherID(String teacherID) {
        this.teacherID = teacherID;
    }

    public String getClassroomID() {
        return classroomID;
    }

    public void setClassroomID(String classroomID) {
        this.classroomID = classroomID;
    }

    public List<String> getStudentUsernames() {
        return studentUsernames;
    }

    public void setStudentUsernames(List<String> studentUsernames) {
        this.studentUsernames = studentUsernames;
    }

}
