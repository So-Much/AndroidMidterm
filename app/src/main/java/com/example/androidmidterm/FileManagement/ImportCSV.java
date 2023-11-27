package com.example.androidmidterm.FileManagement;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.example.androidmidterm.StudentFeature.Certificate.CertificateModel;
import com.example.androidmidterm.StudentFeature.StudentModel;
import com.example.androidmidterm.Utils.FireStoreAction;
import com.google.firebase.firestore.FirebaseFirestore;
import com.opencsv.CSVReader;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ImportCSV {
    private static final String TAG = "ImportCSV";
    Context mContext;
    FireStoreAction fireStoreAction = new FireStoreAction();
    ArrayList<StudentModel> students = new ArrayList<>();
    ArrayList<CertificateModel> certificates = new ArrayList<>();



    public ImportCSV() {
    }

    public ImportCSV(Context context) {
        mContext = context;
    }

    public void importStudents(Uri uriFile, ImportStudentListener importCSVListener) {
        students.clear();
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(mContext.getContentResolver().openInputStream(uriFile)));
            List<String[]> data = reader.readAll();

            for (String[] row : data) {
                if (row.length != 3) continue;
                Double studentGPA;
                try {
                    studentGPA = Double.parseDouble(row[2]);
                } catch (Exception e) {
                    continue;
                }
                String studentName = row[0];
                String studentID = row[1];
                StudentModel student = new StudentModel(studentName, studentID, studentGPA);
                students.add(student);
            }
            FirebaseFirestore.getInstance()
                    .collection("Students")
                    .add(students)
                    .addOnSuccessListener(documentReference -> {
                        Log.e(TAG, "importStudents: Successful" );
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "importStudents: " + e.getMessage());
                    });
            reader.close();
            importCSVListener.onImportStudentSuccess(students);
        } catch (Exception e) {
            Log.e(TAG, "importStudents: " + e.getMessage());
            importCSVListener.onImportStudentFailure();
        };
    }

    public void importCertificates(Uri uriFile, ImportCertificateListener importCSVListener) {
        certificates.clear();
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(mContext.getContentResolver().openInputStream(uriFile)));
            List<String[]> data = reader.readAll();

            for (String[] row : data) {
                if (row.length != 4) continue;
                String studentNumber = row[0];
                String certificateName = row[1];
                String certificateDateIssued = row[2];
                String certificateDateExpired = row[3];
                CertificateModel certificate = fireStoreAction.addCertificate(studentNumber, certificateName, certificateDateIssued, certificateDateExpired);
                certificates.add(certificate);
            }
            reader.close();
            importCSVListener.onImportCertificateSuccess(certificates);
        } catch (Exception e) {
            Log.e(TAG, "importCertificates: " + e.getMessage());
            importCSVListener.onImportCertificateFailure();
        };
    }

    public interface ImportStudentListener {
        void onImportStudentSuccess(ArrayList<StudentModel> students);

        void onImportStudentFailure();
    }
    public interface ImportCertificateListener {
        void onImportCertificateSuccess(ArrayList<CertificateModel> certificates);

        void onImportCertificateFailure();
    }
}
