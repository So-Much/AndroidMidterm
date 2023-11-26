package com.example.androidmidterm.StudentFeature;

public class StudentModel {
    private String studentName;
    private String studentNumber;
    private Double studentGPA;

    public StudentModel() {
    }

    public StudentModel(String studentName, String studentNumber, Double studentGPA) {
        this.studentName = studentName;
        this.studentNumber = studentNumber;
        this.studentGPA = studentGPA;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public Double getStudentGPA() {
        return studentGPA;
    }

    public void setStudentGPA(Double studentGPA) {
        this.studentGPA = studentGPA;
    }
}
