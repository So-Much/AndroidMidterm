package com.example.androidmidterm.StudentFeature;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidmidterm.R;
import com.example.androidmidterm.StudentFeature.Certificate.AdapterCertificateList;
import com.example.androidmidterm.StudentFeature.Certificate.CertificateModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StudentDetailActivity extends AppCompatActivity {
    private static final String TAG = "StudentDetailActivity";
    TextView tvStudentName, tvStudentNumber, tvStudentGPA;
    AppCompatButton btnAddCertificate, btnScanCertificateCSV;
    RecyclerView rvCertificateList;
    Intent dataGot;
    ListenerRegistration listenerRegistration;
    AdapterCertificateList adapterCertificateList;
    ArrayList<CertificateModel> certificates;
    EditText etCertificateName, etIssuedDay, etIssuedMonth, etIssuedYear, etExpiredDay, etExpiredMonth, etExpiredYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentNumber = findViewById(R.id.tvStudentNumber);
        tvStudentGPA = findViewById(R.id.tvStudentGPA);
        btnAddCertificate = findViewById(R.id.btnAddCertificate);
        rvCertificateList = findViewById(R.id.rvCertificateList);
        btnScanCertificateCSV = findViewById(R.id.btnScanCertificateCSV);

        dataGot = getIntent();
        tvStudentName.setText(dataGot.getStringExtra("studentName"));
        tvStudentNumber.setText(dataGot.getStringExtra("studentNumber"));
        tvStudentGPA.setText(""+dataGot.getDoubleExtra("studentGPA", 0));

        btnAddCertificate.setOnClickListener(
                v -> {
                    new AlertDialog.Builder(this)
                            .setView(R.layout.dialog_add_certificate)
                            .setPositiveButton("Yes", (dialog, which) -> {
//                                add certificate
                                etCertificateName = ((AlertDialog) dialog).findViewById(R.id.etCertificateName);
                                etIssuedDay = ((AlertDialog) dialog).findViewById(R.id.etIssuedDay);
                                etIssuedMonth = ((AlertDialog) dialog).findViewById(R.id.etIssuedMonth);
                                etIssuedYear = ((AlertDialog) dialog).findViewById(R.id.etIssuedYear);
                                etExpiredDay = ((AlertDialog) dialog).findViewById(R.id.etExpiredDay);
                                etExpiredMonth = ((AlertDialog) dialog).findViewById(R.id.etExpiredMonth);
                                etExpiredYear = ((AlertDialog) dialog).findViewById(R.id.etExpiredYear);

                                String certificateName = etCertificateName.getText().toString();
                                String certificateDateIssued = etIssuedDay.getText().toString() + "/" + etIssuedMonth.getText().toString() + "/" + etIssuedYear.getText().toString();
                                String certificateDateExpired = etExpiredDay.getText().toString() + "/" + etExpiredMonth.getText().toString() + "/" + etExpiredYear.getText().toString();

                                String StudentID = getIntent().getStringExtra("studentNumber");

                                addCertificate(StudentID, certificateName, certificateDateIssued, certificateDateExpired);

                            })
                            .setNegativeButton("No", null)
                            .show();
                });
        btnScanCertificateCSV.setOnClickListener(
                view -> {

                }
        );

//        showCertificateList
        certificates = new ArrayList<>();
        adapterCertificateList = new AdapterCertificateList(this, certificates);
        rvCertificateList.setAdapter(adapterCertificateList);
        rvCertificateList.setLayoutManager(new LinearLayoutManager(this));
        listenerRegistration = FirebaseFirestore.getInstance()
                .collection("Students")
                .document(dataGot.getStringExtra("studentNumber"))
                .collection("Certificates")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Reading Certificates Failure!.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    certificates.clear();
                    value.forEach(documentSnapshot -> {
                        Log.e(TAG, "Show certificate: " );
                        CertificateModel certificate = documentSnapshot.toObject(CertificateModel.class);
                        Log.e(TAG, "onCreate: "+ certificate.getCertificateName() + " " + certificate.getCertificateDateIssued() + " " + certificate.getCertificateDateExpired() + " " + certificate.getStudentID());
                        certificates.add(certificate);
                    });
                    adapterCertificateList.notifyDataSetChanged();
                });
    }
    private void addCertificate(String studentID, String certificateName, String certificateDateIssued, String certificateDateExpired) {
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
                    updateIDForCertificate(documentReference.getId(), studentID); // studentID
                });
    }
    private void updateIDForCertificate(String certificateID, String studentID) { // studentID
        FirebaseFirestore.getInstance()
                .collection("Students")
                .document(studentID) // studentID
                .collection("Certificates")
                .document(certificateID)
                .update("certificateID", certificateID)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Add Certificate Successful!", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        listenerRegistration.remove();
        super.onDestroy();
    }
}