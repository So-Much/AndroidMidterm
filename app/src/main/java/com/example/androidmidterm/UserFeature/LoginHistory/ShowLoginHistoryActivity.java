package com.example.androidmidterm.UserFeature.LoginHistory;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidmidterm.R;
import com.example.androidmidterm.UserFeature.LoginHistory.LoginHistoryAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Date;

public class ShowLoginHistoryActivity extends AppCompatActivity {
    private static final String TAG = "ShowLoginHistoryActivity";
    private RecyclerView recyclerViewLoginHistory;
    LoginHistoryAdapter adapter;
    ListenerRegistration listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_login_history);

        recyclerViewLoginHistory = findViewById(R.id.recyclerViewLoginHistory);
        recyclerViewLoginHistory.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<Date> loginHistories = new ArrayList<>();
        adapter = new LoginHistoryAdapter(this, loginHistories);
        listener = FirebaseFirestore.getInstance().collection("Users")
                .document(getIntent().getStringExtra("userID"))
                .collection("loginHistory")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore error", error.getMessage());
                        return;
                    }
                    loginHistories.clear();
                    for (int i = 0; i < value.getDocuments().size(); i++) {
                        loginHistories.add(value.getDocuments().get(i).getDate("time"));
                        Log.e(TAG, "onCreate: " + value.getDocuments().get(i).getDate("time"));
                    }
                    adapter.notifyDataSetChanged();
                });
        recyclerViewLoginHistory.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        listener.remove();
        super.onDestroy();
    }
}