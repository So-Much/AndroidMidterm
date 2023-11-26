package com.example.androidmidterm.UserFeature;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidmidterm.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserFragment extends Fragment {
    private static final String TAG = "UserFragment";
    UserModel currentUser;
    ArrayList<UserModel> users = new ArrayList<>();
    FloatingActionButton fab;
    FirebaseAuth mAuth;
    RecyclerView rvUserList;
    AdapterUserList adapterUserList;
    private ListenerRegistration listenerRegistration;
    ActivityResultLauncher<Intent> launcherUpdateUser = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == 1) {
//                    update into adapter data
                    Intent data = result.getData();
                    Log.e(TAG, "onCreateView: " + result.getData().getIntExtra("userPos", 0));
                    UserModel userUpdate = users.get(data.getIntExtra("userPos", 0));
                    userUpdate.setUserName(data.getStringExtra("userName"));
                    userUpdate.setUserAge(data.getIntExtra("userAge", 0));
                    userUpdate.setUserPhone(data.getStringExtra("userPhone"));
                    userUpdate.setUserRoleWithString(data.getStringExtra("userRole"));
                    userUpdate.setUserStatus(data.getBooleanExtra("userStatus", false));
                    userUpdate.setUserImage(data.getStringExtra("userImage"));
                    userUpdate.setUserID(users.get(data.getIntExtra("userPos", 0)).getUserID());
                    userUpdate.setLoginHistory(users.get(data.getIntExtra("userPos", 0)).getLoginHistory());
                    users.set(data.getIntExtra("userPos", 0), userUpdate);
                    adapterUserList.notifyDataSetChanged();

//                    update into firebase
                    FirebaseFirestore.getInstance().collection("Users").document(userUpdate.getUserID())
                            .set(userUpdate);
                }
            });

    public UserFragment() {
    }

    public UserFragment(UserModel currentUser) {
        this.currentUser = currentUser;
    }

    public static UserFragment newInstance(UserModel currentUser) {
        UserFragment fragment = new UserFragment(currentUser);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance(); // get instance of firebase auth
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        fab = view.findViewById(R.id.fab);
        rvUserList = view.findViewById(R.id.rvUserList);
        adapterUserList = new AdapterUserList(view.getContext(), users, launcherUpdateUser);

        fab.setOnClickListener(viewFab -> {
            Intent intent = new Intent(view.getContext(), PostUserActivity.class);
            startActivity(intent);
        });

        listenerRegistration = FirebaseFirestore.getInstance().collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e(TAG, "Listen failed." + error.getMessage());
                    return;
                }
                users.clear();
                if (value != null && !value.isEmpty()) {
                    List<DocumentSnapshot> documents = value.getDocuments();
                    for (DocumentSnapshot document : documents) {
                        UserModel userModel = document.toObject(UserModel.class);
                        if (userModel != null) {
                            users.add(userModel);
                        }
                    }
                }

                // Assuming you have a reference to your adapter (adapterUserList)
                if (adapterUserList != null) {
                    adapterUserList.notifyDataSetChanged();
                }
            }

        });
        for (UserModel user : users) {
            Log.e(TAG, "onCreateView: " + user.getUserID());
        }
        rvUserList.setAdapter(adapterUserList);
        rvUserList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        return view;
    }
}