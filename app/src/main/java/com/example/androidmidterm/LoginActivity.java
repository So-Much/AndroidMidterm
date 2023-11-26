package com.example.androidmidterm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    EditText etEmail, etPassword;
    Button btnLogin;
    FirebaseAuth mAuth;
    SharedPreferences sharedPreferences;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance(); // get instance of firebase auth
        sharedPreferences = getSharedPreferences("MainPref", Context.MODE_PRIVATE);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new AlertDialog.Builder(LoginActivity.this)
                        .setCancelable(false)
                        .setView(R.layout.progress_layout)
                        .create();
                String email = etEmail.getText().toString().trim(); // trim() removes spaces
                String password = etPassword.getText().toString();
                if (!email.isEmpty() || !password.isEmpty()) {
                    dialog.show();
                    loginUser(email, password);
                } else {
                    etEmail.setError("Please enter email");
                    etPassword.setError("Please enter password");
                }
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                Log.e(TAG, "loginUser: " + mAuth.getCurrentUser().getUid());
                Map<String, Object> loginData = new HashMap<>();
                loginData.put("time", FieldValue.serverTimestamp());
                FirebaseFirestore.getInstance().collection("Users")
                        .document(mAuth.getCurrentUser().getUid())
                        .collection("loginHistory")
                        .add(loginData)
                        .addOnSuccessListener(documentReference -> {Log.e(TAG, "loginUser: " + documentReference.getId());})
                        .addOnFailureListener(e -> {Log.e(TAG, "loginUser: " + e.getMessage());});

                sharedPreferences.edit()
                        .putString("email", email)
                        .putString("password", password)
                        .putString("userID", mAuth.getCurrentUser().getUid())
                        .apply();
                dialog.dismiss();
                startActivity(intent);
                finish();
            } else {
                dialog.dismiss();
                Toast.makeText(this, "Email or Password is wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}