package com.example.androidmidterm.UserFeature;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidmidterm.MainActivity;
import com.example.androidmidterm.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostUserActivity extends AppCompatActivity {
    private static final String TAG = "PostUserActivity";
    private Context mContext = PostUserActivity.this;
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    CircleImageView userImage;
    EditText etEmail, etPassword, etUserName, etUserAge, etUserPhone;
    RadioGroup rgRoles;
    RadioButton rbRole;
    Switch swStatus; // true = blocked, false = unblocked
    Button btnPostUser;
    Uri imageUri;
    String imgURL;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_user);

        mAuth = FirebaseAuth.getInstance();

        userImage = findViewById(R.id.userImage);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etUserName = findViewById(R.id.etUserName);
        etUserAge = findViewById(R.id.etUserAge);
        etUserPhone = findViewById(R.id.etUserPhone);
        rgRoles = findViewById(R.id.rgRoles);
        swStatus = findViewById(R.id.swStatus);
        btnPostUser = findViewById(R.id.btnPostUser);

        imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.user_icon)
                + '/' + getResources().getResourceTypeName(R.drawable.user_icon) + '/' + getResources().getResourceEntryName(R.drawable.user_icon));


        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        imageUri = data.getData();
                        userImage.setImageURI(imageUri);
                    } else {
                        Toast.makeText(this, "No image is selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        userImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            activityResultLauncher.launch(intent);
        });

        btnPostUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String userName = etUserName.getText().toString().trim();
                int userAge = Integer.parseInt(etUserAge.getText().toString().trim());
                String userPhone = etUserPhone.getText().toString().trim();
                Date currentTime = Calendar.getInstance().getTime();

//        validate input
                if (email.isEmpty()) {
                    etEmail.setError("Email is required");
                    return;
                } else if (!VALID_EMAIL_ADDRESS_REGEX.matcher(email).find()) {
                    etEmail.setError("Invalid email");
                    return;
                } else {
                    etEmail.setError(null);
                }
                if (password.isEmpty()) {
                    etPassword.setError("Password is required");
                    return;
                } else if (password.length() < 6) {
                    etPassword.setError("Password must be at least 6 characters");
                    return;
                } else {
                    etPassword.setError(null);
                }
//                confirmation dialog
                new AlertDialog.Builder(mContext)
                        .setTitle("Confirmation")
                        .setMessage("Are you sure you want to add this User?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (rgRoles.getCheckedRadioButtonId() == -1) {
                                    Toast.makeText(mContext, "Please select a role", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                registerUser(email, password, new RegistrationCallback() {
                                    @Override
                                    public void onSuccess(boolean isSuccess, String userID) {
                                        if (isSuccess) {
                                            USER_ROLE userRole = USER_ROLE.EMPLOYEE;
                                            int selectedRole = rgRoles.getCheckedRadioButtonId();
                                            rbRole = findViewById(selectedRole);
                                            switch (rbRole.getText().toString()) {
                                                case "ADMIN":
                                                    userRole = USER_ROLE.ADMIN;
                                                    break;
                                                case "MANAGER":
                                                    userRole = USER_ROLE.MANAGER;
                                                    break;
                                                case "EMPLOYEE":
                                                    userRole = USER_ROLE.EMPLOYEE;
                                                    break;
                                                default:
                                                    break;
                                            }
                                            addUser(userID, userRole, userName, userAge, userPhone, currentTime);
                                        } else {
                                            Toast.makeText(mContext, "Failed to register user", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    private void addUser(String userID, USER_ROLE userRole, String userName, int userAge, String userPhone, Date currentTime) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("Midterm_Android_User_Image").child(userID).child(imageUri.getLastPathSegment());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(R.layout.progress_layout)
                .create();
        dialog.show();
        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isComplete()) ;
                        Uri uri = uriTask.getResult();
                        imgURL = uri.toString();
                        ArrayList<UserModel.LoginHistory> loginHistory = new ArrayList<>();
                        loginHistory.add(new UserModel.LoginHistory(currentTime));
                        UserModel userModel = new UserModel(userID, userRole, userName, userAge, userPhone, swStatus.isChecked(), imgURL,loginHistory);

                        FirebaseFirestore.getInstance().collection("Users").document(userID)
                                .set(userModel)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(PostUserActivity.this, "Add User Successfully!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(PostUserActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(PostUserActivity.this, "Failed to add User!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "onFailure: " + e.getMessage());
                                    }
                                });
                        dialog.dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });

    }

    private void registerUser(String email, String password, RegistrationCallback callback) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(R.layout.progress_layout)
                .create();
        dialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(mContext, "User registered successfully", Toast.LENGTH_SHORT).show();
                            String userID = mAuth.getUid();
                            dialog.dismiss();
                            callback.onSuccess(true, userID);
                        } else {
                            Toast.makeText(mContext, "Failed to register user", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(e -> {
                    dialog.dismiss();
                    Log.e(TAG, "addUser: " + e.getMessage());
                    Toast.makeText(mContext, "Account is existed!", Toast.LENGTH_SHORT).show();
                });
    }

    public interface RegistrationCallback {
        void onSuccess(boolean isSuccess, String userID);
    }
}