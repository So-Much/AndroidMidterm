package com.example.androidmidterm;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.androidmidterm.UserFeature.USER_ROLE;
import com.example.androidmidterm.UserFeature.UpdateUserActivity;
import com.example.androidmidterm.UserFeature.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingFragment extends Fragment {
    private static final String TAG = SettingFragment.class.getSimpleName();
    UserModel currentUser;
    FirebaseAuth mAuth;
    View view;
    TextView tvUserName, tvUserAge, tvUserPhone, tvUserRole, tvUserIsActive;
    CircleImageView userImage;
    AppCompatButton btnEditUser, btnDeleteUser, btnLogout;
    ActivityResultLauncher<Intent> launcherUpdateUser = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == 1) {
//                    update into adapter data
                    Intent data = result.getData();
                    UserModel userUpdate = new UserModel();
                    userUpdate.setUserName(data.getStringExtra("userName"));
                    userUpdate.setUserAge(data.getIntExtra("userAge", 0));
                    userUpdate.setUserPhone(data.getStringExtra("userPhone"));
                    userUpdate.setUserRoleWithString(data.getStringExtra("userRole"));
                    userUpdate.setUserStatus(data.getBooleanExtra("userStatus", false));
                    userUpdate.setUserImage(data.getStringExtra("userImage"));
                    userUpdate.setUserID(currentUser.getUserID());
                    userUpdate.setLoginHistory(currentUser.getLoginHistory());

                    currentUser = userUpdate;
                    updateUI(currentUser);

//                    update into firebase
                    FirebaseFirestore.getInstance().collection("Users").document(currentUser.getUserID())
                            .set(userUpdate);
                }
            });

    public SettingFragment() {
        // Required empty public constructor
    }

    public SettingFragment(UserModel currentUser) {
        this.currentUser = currentUser;
    }

    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_setting, container, false);
        mAuth = FirebaseAuth.getInstance(); // get instance of firebase auth

        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserAge = view.findViewById(R.id.tvUserAge);
        tvUserPhone = view.findViewById(R.id.tvUserPhone);
        tvUserRole = view.findViewById(R.id.tvUserRole);
        tvUserIsActive = view.findViewById(R.id.tvUserIsActive);
        userImage = view.findViewById(R.id.userImage);
        btnEditUser = view.findViewById(R.id.btnEditUser);
        btnDeleteUser = view.findViewById(R.id.btnDeleteUser);
        btnLogout = view.findViewById(R.id.btnLogOut);

        updateUI(currentUser);

        btnDeleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Delete User")
                        .setMessage("Are you sure you want to delete this user?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // delete user
                            deleteUser(view);
                            getActivity().finish();
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // logout
                            mAuth.signOut();
                            Intent intent = new Intent(view.getContext(), LoginActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        btnEditUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launcherUpdateUser.launch(new Intent(view.getContext(), UpdateUserActivity.class)
                        .putExtra("userID", currentUser.getUserID())
                        .putExtra("userName", currentUser.getUserName())
                        .putExtra("userAge", currentUser.getUserAge())
                        .putExtra("userPhone", currentUser.getUserPhone())
                        .putExtra("userRole", currentUser.getUserRole().toString())
                        .putExtra("userStatus", currentUser.isUserStatus())
                        .putExtra("userImage", currentUser.getUserImage()));
            }
        });
        return view;
    }

    private void deleteUser(View view) {
        // delete user from firebase
        if (mAuth.getCurrentUser() != null) {
            // delete user from firestore
            FirebaseFirestore.getInstance().collection("Users").document(currentUser.getUserID()).delete();
            // delete user from storage
            FirebaseStorage.getInstance().getReference("Midterm_Android_User_Image").child(currentUser.getUserID()).delete();
            // delete user from auth
            mAuth.getCurrentUser().delete();
            // delete user from UI
            Intent intent = new Intent(view.getContext(), LoginActivity.class);
            startActivity(intent);
            Toast.makeText(view.getContext(), "User deleted successfully", Toast.LENGTH_SHORT).show();
        } else {
            // user is not logged in
            Toast.makeText(view.getContext(), "User is not logged in", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "deleteUser: user is not logged in");
        }
    }
    private void updateUI(UserModel currentUser) {
        tvUserName.setText(currentUser.getUserName());
        tvUserAge.setText("" + currentUser.getUserAge());
        tvUserPhone.setText(currentUser.getUserPhone());
        tvUserRole.setText(currentUser.getUserRole().toString());
        tvUserIsActive.setText(!currentUser.isUserStatus() ? "Active" : "Inactive");
        Glide.with(view.getContext()).load(currentUser.getUserImage()).into(userImage);
    }
}