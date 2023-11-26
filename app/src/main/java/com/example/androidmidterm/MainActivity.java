package com.example.androidmidterm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.androidmidterm.StudentFeature.StudentFragment;
import com.example.androidmidterm.UserFeature.USER_ROLE;
import com.example.androidmidterm.UserFeature.UserFragment;
import com.example.androidmidterm.UserFeature.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    SharedPreferences sharedPreferences;
    //    private ListenerRegistration listenerRegistration;
    String currentEmail, currentPassword;
    BottomNavigationView bottomNavigationView;
    UserModel currentUser = new UserModel();
    ArrayList<UserModel> users = new ArrayList<>();
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance(); // get instance of firebase auth
        sharedPreferences = getSharedPreferences("MainPref", MODE_PRIVATE);

        reLogin(new OnSignInListener() {
            @Override
            public void onSignIn() {
                Log.e(TAG, "onSignIn: success");
                bottomNavigationView = findViewById(R.id.bottomNavigationView);
                FirebaseFirestore.getInstance().collection("Users")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                users.clear();
                                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                                    Log.e(TAG, "onEvent: " + documents.size());
                                    for (DocumentSnapshot document : documents) {
                                        UserModel userModel = document.toObject(UserModel.class);
                                        Log.e(TAG, "onEvent: userModel: " + userModel.getUserID() + "\n mAuth_UserID: " + mAuth.getCurrentUser().getUid());
                                        if (userModel != null && userModel.getUserID().equals(mAuth.getCurrentUser().getUid())) {
                                            currentUser = userModel;
                                            replaceFragment(new SettingFragment(currentUser));
                                            break;
                                        }
                                    }
                                }
                            }
                        });
                bottomNavigationView.setOnItemSelectedListener(item -> {
                    if ((item.getItemId() == R.id.user_fragment)
                            && currentUser.getUserRole().equals(USER_ROLE.ADMIN)) {
                        replaceFragment(new UserFragment(currentUser));
                        return true;
                    } else if (item.getItemId() == R.id.studen_fragment
                            && (currentUser.getUserRole().equals(USER_ROLE.MANAGER)
                            || currentUser.getUserRole().equals(USER_ROLE.ADMIN))) {
                        replaceFragment(new StudentFragment(currentUser));
                        return true;
                    } else if (item.getItemId() == R.id.setting_fragment) {
                        replaceFragment(new SettingFragment(currentUser));
                        return true;
                    } else {
                        Toast.makeText(MainActivity.this, "You don't have permission to access this feature", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
            }
        });


//        listenerRegistration = FirebaseFirestore.getInstance().collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                if (error != null) {
//                    Log.e(TAG, "onEvent: " + error.getMessage() );
//                    return;
//                }
//                users.clear();
//                if (value != null && !value.isEmpty()) {
//                    List<DocumentSnapshot> documents = value.getDocuments();
//                    Log.e(TAG, "onEvent: "+ documents.size());
//                    for (DocumentSnapshot document : documents) {
//                        UserModel userModel = document.toObject(UserModel.class);
//                        Log.e(TAG, "onEvent: userModel: "+ userModel.getUserID() + "\n mAuth_UserID: " + mAuth.getCurrentUser().getUid());
//                        if (userModel != null && userModel.getUserID().equals(mAuth.getCurrentUser().getUid())) {
//                            currentUser = userModel;
//                            break;
//                        }
//                    }
//                }
//            }
//        });
//        getAllUsers and check current user roles

    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    public void reLogin(OnSignInListener listener) {
        currentEmail = sharedPreferences.getString("email", "");
        currentPassword = sharedPreferences.getString("password", "");
        currentUser.setUserID(sharedPreferences.getString("userID", ""));
        if (mAuth.getCurrentUser() != null) {
            if (!mAuth.getCurrentUser().getUid().equals(currentUser.getUserID())) {
                mAuth.signOut();
                mAuth.signInWithEmailAndPassword(currentEmail, currentPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                listener.onSignIn();
                            } else {
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                                Log.e(TAG, "reDirect: failed");
                            }
                        });
            } else if (mAuth.getCurrentUser().getUid().equals(currentUser.getUserID())){
                listener.onSignIn();
            }
        } else {
            mAuth.signInWithEmailAndPassword(currentEmail, currentPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            listener.onSignIn();
                            Log.e(TAG, "reDirect: success");
                        } else {
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                            Log.e(TAG, "reDirect: failed");
                        }
                    });
        }
    }
    public interface OnSignInListener {
        void onSignIn();
    }
}