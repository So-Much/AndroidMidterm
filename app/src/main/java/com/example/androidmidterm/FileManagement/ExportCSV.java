package com.example.androidmidterm.FileManagement;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.example.androidmidterm.StudentFeature.Certificate.CertificateModel;
import com.example.androidmidterm.StudentFeature.StudentModel;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class ExportCSV {
    Context mContext;
    private static final String TAG = "ExportCSV Task";

    public ExportCSV() {
    }

    public ExportCSV(Context mContext){this.mContext = mContext;}
    public void exportStudents(ArrayList<StudentModel> students, DocumentFile file, ExportCSVListener exportCSVListener){
//        filename = "/storage/emulated/0/tree/primary/TestCSV/students.csv"
        try {
            if (file.exists()) {
                CSVWriter writer = new CSVWriter(new OutputStreamWriter(mContext.getContentResolver().openOutputStream(file.getUri())));
                String title[] = {"Student Name", "Student Number", "Student GPA"};
                writer.writeNext(title);
                for (StudentModel student : students) {
                    String[] data = {student.getStudentName(), student.getStudentNumber(), student.getStudentGPA().toString()};
                    writer.writeNext(data);
                }
                writer.close();
                exportCSVListener.onExportCSVSuccess();
            }else {
                exportCSVListener.onExportCSVFailure();
            }
        } catch (IOException e) {
            Log.e(TAG, "exportStudents: " +e.getMessage() );
        }
    }
    public void exportCertificates(ArrayList<CertificateModel> certificates, DocumentFile file, ExportCSVListener exportCSVListener){
        try {
            if (file.exists()) {
                CSVWriter writer = new CSVWriter(new OutputStreamWriter(mContext.getContentResolver().openOutputStream(file.getUri())));
                String title[] = {"Student Number", "Certificate Name", "Date Issued", "Date Expired"};
                writer.writeNext(title);
                for (CertificateModel certificate : certificates) {
                    Log.e(TAG, "exportCertificates: "+certificate.getStudentID()+", "+certificate.getCertificateName()+", "+certificate.getCertificateDateIssued()+", "+certificate.getCertificateDateExpired() );
                    String[] data = {certificate.getStudentID(), certificate.getCertificateName(), certificate.getCertificateDateIssued(), certificate.getCertificateDateExpired()};
                    writer.writeNext(data);
                }
                writer.close();
                exportCSVListener.onExportCSVSuccess();
            }else {
                exportCSVListener.onExportCSVFailure();
            }
        } catch (IOException e) {
            Log.e(TAG, "exportStudents: " +e.getMessage() );
        }
    }

    public interface ExportCSVListener{
        void onExportCSVSuccess();
        void onExportCSVFailure();
    }
}
