package com.example.androidmidterm.StudentFeature.Certificate;

import java.util.Date;

public class CertificateModel {
    private String StudentID;
    private String certificateID;
    private String certificateName;
    private String certificateDateIssued;
    private String certificateDateExpired;

    public CertificateModel() {
    }

    public CertificateModel(String studentID, String certificateID, String certificateName, String certificateDateIssued, String certificateDateExpired) {
        StudentID = studentID;
        this.certificateID = certificateID;
        this.certificateName = certificateName;
        this.certificateDateIssued = certificateDateIssued;
        this.certificateDateExpired = certificateDateExpired;
    }

    public String getStudentID() {
        return StudentID;
    }

    public void setStudentID(String studentID) {
        StudentID = studentID;
    }

    public String getCertificateID() {
        return certificateID;
    }

    public void setCertificateID(String certificateID) {
        this.certificateID = certificateID;
    }

    public String getCertificateName() {
        return certificateName;
    }

    public void setCertificateName(String certificateName) {
        this.certificateName = certificateName;
    }

    public String getCertificateDateIssued() {
        return certificateDateIssued;
    }

    public void setCertificateDateIssued(String certificateDateIssued) {
        this.certificateDateIssued = certificateDateIssued;
    }

    public String getCertificateDateExpired() {
        return certificateDateExpired;
    }

    public void setCertificateDateExpired(String certificateDateExpired) {
        this.certificateDateExpired = certificateDateExpired;
    }
}
