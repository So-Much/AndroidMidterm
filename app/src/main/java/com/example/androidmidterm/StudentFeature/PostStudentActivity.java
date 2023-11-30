package com.example.androidmidterm.StudentFeature;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.androidmidterm.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class PostStudentActivity extends AppCompatActivity {
    private static final String TAG = "PostStudentActivity";
    TextView tvTitle;
    TextInputEditText etStudentName, etStudentNumber, etStudentGPA;
    AppCompatButton btnPostStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_student);

        etStudentName = findViewById(R.id.etStudentName);
        etStudentNumber = findViewById(R.id.etStudentNumber);
        etStudentGPA = findViewById(R.id.etStudentGPA);
        btnPostStudent = findViewById(R.id.btnPostStudent);
        tvTitle = findViewById(R.id.tvTitle);
        if (getIntent().getData() != null) {
            Intent getIntent = getIntent();
            etStudentName.setText(getIntent.getStringExtra("studentName"));
            etStudentNumber.setText(getIntent.getStringExtra("studentNumber"));
            etStudentGPA.setText("" + getIntent.getDoubleExtra("studentGPA", 0.0));
            btnPostStudent.setText("Update Student");
            tvTitle.setText("Update Student");
        }


        btnPostStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String studentName = etStudentName.getText().toString().trim();
                String studentNumber = etStudentNumber.getText().toString().trim();
                Double studentGPA;
                try {
                    studentGPA = Double.parseDouble(etStudentGPA.getText().toString().trim());
                } catch (NumberFormatException e) {
                    etStudentGPA.setError("Student GPA is invalid!");
                    etStudentGPA.requestFocus();
                    return;
                }
                validateInput(studentName, studentNumber, studentGPA);

                StudentModel newStudent = new StudentModel(studentName, studentNumber, studentGPA);
                if (getIntent().getData() != null) {
                    if (getIntent().getStringExtra("studentNumber").equals(newStudent.getStudentNumber())) {
                        FirebaseFirestore.getInstance().collection("Students")
                                .document(studentNumber)
                                .set(newStudent)
                                .addOnSuccessListener(new OnSuccessListener() {
                                    @Override
                                    public void onSuccess(Object o) {
                                        Toast.makeText(PostStudentActivity.this, "Student updated successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                    } else {
                        FirebaseFirestore.getInstance()
                                .collection("Students")
                                .document(getIntent().getStringExtra("studentNumber"))
                                .delete()
                                .addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                    }
                                });
                        FirebaseFirestore.getInstance()
                                .collection("Students")
                                .document(newStudent.getStudentNumber())
                                .set(newStudent)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(PostStudentActivity.this, "Student updated successfully", Toast.LENGTH_SHORT).show();
                                        Intent getIntent = getIntent();
                                        setResult(RESULT_OK, getIntent);
                                        finish();
                                    } else {
                                        Toast.makeText(PostStudentActivity.this, "Student update failed", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }

                } else {
//                    postStudent
//                    check if studentNumber already exists and add or not
                    FirebaseFirestore.getInstance().collection("Students")
                            .whereEqualTo("studentNumber", studentNumber)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                    if studentNumber already exists
                                    if (queryDocumentSnapshots.size() > 0) {
                                        Toast.makeText(PostStudentActivity.this, "Student Number already exists", Toast.LENGTH_SHORT).show();
                                    } else {
//                                        if studentNumber does not exist
                                        FirebaseFirestore.getInstance().collection("Students")
                                                .document(studentNumber)
                                                .set(newStudent)
                                                .addOnSuccessListener(new OnSuccessListener() {
                                                    @Override
                                                    public void onSuccess(Object o) {
                                                        Toast.makeText(PostStudentActivity.this, "Student added successfully", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                });
                                    }
                                }
                            });
                }
            }
        });
    }

    private boolean validateInput(String stdName, String stdNumber, Double stdGPA) {
        if (stdName.isEmpty()) {
            etStudentName.setError("Student name cannot be empty");
            etStudentName.requestFocus();
            return false;
        } else {
            etStudentName.setError(null);
        }
        if (stdNumber.isEmpty()) {
            etStudentNumber.setError("Student number cannot be empty");
            etStudentNumber.requestFocus();
            return false;
        } else {
            etStudentNumber.setError(null);
        }
        if (stdGPA == null) {
            etStudentGPA.setError("Student GPA cannot be empty");
            etStudentGPA.requestFocus();
            return false;
        } else {
            etStudentGPA.setError(null);
        }
        return true;
    }
}