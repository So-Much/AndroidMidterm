package com.example.androidmidterm.Utils;

import android.widget.Toast;

import com.example.androidmidterm.StudentFeature.Certificate.CertificateModel;
import com.example.androidmidterm.StudentFeature.StudentModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FireStoreAction {
    public FireStoreAction() {
    }
    public boolean isExitsStudentNumber(String studentNumber) {
        ArrayList<StudentModel> students = new ArrayList<>();
        FirebaseFirestore.getInstance()
                .collection("Students")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        StudentModel student = queryDocumentSnapshots.getDocuments().get(i).toObject(StudentModel.class);
                        students.add(student);
                    }
                });
        for (StudentModel student : students) {
            if (student.getStudentNumber().equals(studentNumber)) {
                return true;
            }
        }
        return false;
    }
    public CertificateModel addCertificate(String studentID, String certificateName, String certificateDateIssued, String certificateDateExpired) {
        CertificateModel certificateModel = new CertificateModel();
        Map<String, Object> certificate = new HashMap<>();
        certificate.put("StudentID", studentID);
        certificate.put("certificateName", certificateName);
        certificate.put("certificateDateIssued", certificateDateIssued);
        certificate.put("certificateDateExpired", certificateDateExpired);
        FirebaseFirestore.getInstance()
                .collection("Students")
                .document(studentID) // studentID
                .collection("Certificates")
                .add(certificate)
                .addOnSuccessListener(documentReference -> {
                    certificateModel.setCertificateID(documentReference.getId());
                    certificateModel.setCertificateName(certificateName);
                    certificateModel.setCertificateDateIssued(certificateDateIssued);
                    certificateModel.setCertificateDateExpired(certificateDateExpired);
                    updateIDForCertificate(documentReference.getId(), studentID); // studentID
                });
        return certificateModel;
    }
    private void updateIDForCertificate(String certificateID, String studentID) { // studentID
        FirebaseFirestore.getInstance()
                .collection("Students")
                .document(studentID) // studentID
                .collection("Certificates")
                .document(certificateID)
                .update("certificateID", certificateID);
    }
}
