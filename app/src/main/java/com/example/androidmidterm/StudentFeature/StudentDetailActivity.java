package com.example.androidmidterm.StudentFeature;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidmidterm.FileManagement.ExportCSV;
import com.example.androidmidterm.FileManagement.ImportCSV;
import com.example.androidmidterm.R;
import com.example.androidmidterm.StudentFeature.Certificate.AdapterCertificateList;
import com.example.androidmidterm.StudentFeature.Certificate.CertificateModel;
import com.example.androidmidterm.Utils.FireStoreAction;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public class StudentDetailActivity extends AppCompatActivity {
    private static final String TAG = "StudentDetailActivity";
    TextView tvStudentName, tvStudentNumber, tvStudentGPA;
    AppCompatButton btnAddCertificate, btnImportCertificateCSV, btnExportCertificateCSV;
    RecyclerView rvCertificateList;
    Intent dataGot;
    ListenerRegistration listenerRegistration;
    AdapterCertificateList adapterCertificateList;
    ArrayList<CertificateModel> certificates;
    EditText etCertificateName, etIssuedDay, etIssuedMonth, etIssuedYear, etExpiredDay, etExpiredMonth, etExpiredYear;
    ImportCSV importCSV;
    ExportCSV exportCSV;
    ActivityResultLauncher<Intent> filePickerLauncher, folderPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentNumber = findViewById(R.id.tvStudentNumber);
        tvStudentGPA = findViewById(R.id.tvStudentGPA);
        btnAddCertificate = findViewById(R.id.btnAddCertificate);
        rvCertificateList = findViewById(R.id.rvCertificateList);
        btnImportCertificateCSV = findViewById(R.id.btnImportCertificateCSV);
        btnExportCertificateCSV = findViewById(R.id.btnExportCertificateCSV);
        FireStoreAction fireStoreAction = new FireStoreAction();
        exportCSV = new ExportCSV(this);
        importCSV = new ImportCSV(this);

        dataGot = getIntent();
        tvStudentName.setText(dataGot.getStringExtra("studentName"));
        tvStudentNumber.setText(dataGot.getStringExtra("studentNumber"));
        tvStudentGPA.setText("" + dataGot.getDoubleExtra("studentGPA", 0));

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Uri uri = result.getData().getData();
                        importCSV.importCertificates(uri, new ImportCSV.ImportCertificateListener() {
                            @Override
                            public void onImportCertificateSuccess(ArrayList<CertificateModel> certificatesImported) {
                                certificates.addAll(certificatesImported);
                                adapterCertificateList.notifyDataSetChanged();
                                Toast.makeText(StudentDetailActivity.this, "Import CSV Success", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onImportCertificateFailure() {
                                Toast.makeText(StudentDetailActivity.this, "Import CSV Failure", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        );
        folderPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Uri uri = result.getData().getData();
                        EditText etFileName = new EditText(this);
                        new AlertDialog.Builder(this)
                                .setTitle("Export CSV")
                                .setMessage("Enter file name(.csv):")
                                .setView(etFileName)
                                .setPositiveButton("Export", (dialog, which) -> {
                                    String fileName = etFileName.getText().toString().trim();
                                    if (!fileName.endsWith(".csv")) {
                                        fileName += ".csv";
                                    }
                                    DocumentFile file = DocumentFile.fromTreeUri(this, uri).createFile("text/csv", fileName);
                                    exportCSV.exportCertificates(certificates, file, new ExportCSV.ExportCSVListener() {
                                        @Override
                                        public void onExportCSVSuccess() {
                                            Toast.makeText(StudentDetailActivity.this, "Export CSV Success", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onExportCSVFailure() {
                                            Toast.makeText(StudentDetailActivity.this, "Export CSV Failure", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                }
        );

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
//                                validate
                                if (etCertificateName.getText().toString().isEmpty()
                                        || etIssuedDay.getText().toString().isEmpty()
                                        || etIssuedMonth.getText().toString().isEmpty()
                                        || etIssuedYear.getText().toString().isEmpty()
                                        || etExpiredDay.getText().toString().isEmpty()
                                        || etExpiredMonth.getText().toString().isEmpty()
                                        || etExpiredYear.getText().toString().isEmpty()) {
                                    Toast.makeText(this, "Not enough information!", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(this, "Please fill all the fields and retry!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (Integer.parseInt(etIssuedDay.getText().toString()) > 31
                                        || Integer.parseInt(etIssuedMonth.getText().toString()) > 12
                                        || Integer.parseInt(etIssuedYear.getText().toString()) > 2023
                                        || Integer.parseInt(etExpiredDay.getText().toString()) > 31
                                        || Integer.parseInt(etExpiredMonth.getText().toString()) > 12
                                        || Integer.parseInt(etExpiredYear.getText().toString()) > 2023) {
                                    Toast.makeText(this, "Invalid date!", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(this, "Please fill all the fields and retry!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String certificateName = etCertificateName.getText().toString();
                                String certificateDateIssued = etIssuedDay.getText().toString() + "/" + etIssuedMonth.getText().toString() + "/" + etIssuedYear.getText().toString();
                                String certificateDateExpired = etExpiredDay.getText().toString() + "/" + etExpiredMonth.getText().toString() + "/" + etExpiredYear.getText().toString();

                                String StudentID = getIntent().getStringExtra("studentNumber");

                                fireStoreAction.addCertificate(StudentID, certificateName, certificateDateIssued, certificateDateExpired);

                            })
                            .setNegativeButton("No", null)
                            .show();
                });
        btnImportCertificateCSV.setOnClickListener(
                view -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!Environment.isExternalStorageManager()) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            startActivity(intent);
                        } else {
                            Log.e(TAG, "onCreate: Permission is granted!");
                            Intent filePicker = new Intent(Intent.ACTION_GET_CONTENT);
                            filePicker.addCategory(Intent.CATEGORY_OPENABLE);
                            filePicker.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            filePicker.setType("*/*");
                            filePicker = Intent.createChooser(filePicker, "Select a file");
                            filePickerLauncher.launch(filePicker);
                        }
                    }
                    Log.e(TAG, "onCreate: something went wrong!");
                }
        );
        btnExportCertificateCSV.setOnClickListener(
                view -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Intent folderPicker = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        folderPicker.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        folderPicker.addCategory(Intent.CATEGORY_DEFAULT);
                        folderPickerLauncher.launch(Intent.createChooser(folderPicker, "Select a folder"));
                    }
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
                        CertificateModel certificate = documentSnapshot.toObject(CertificateModel.class);
                        certificates.add(certificate);
                    });
                    adapterCertificateList.notifyDataSetChanged();
                });
    }

    @Override
    protected void onDestroy() {
        listenerRegistration.remove();
        super.onDestroy();
    }
}