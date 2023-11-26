package com.example.androidmidterm.UserFeature.LoginHistory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidmidterm.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LoginHistoryAdapter extends RecyclerView.Adapter<LoginHistoryAdapter.ViewHolder> {
    Context mContext;

    private List<Date> loginHistoryList;

    public LoginHistoryAdapter(Context mContext, List<Date> loginHistoryList) {
        this.mContext = mContext;
        this.loginHistoryList = loginHistoryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_login_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Date loginTime = loginHistoryList.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        String formattedLoginTime = sdf.format(loginTime);
        if (loginHistoryList.size() == 0) {
            holder.textViewLoginTime.setText("No login history");
            return;
        }
        holder.textViewLoginTime.setText("Login Time: " + formattedLoginTime);
    }

    @Override
    public int getItemCount() {
        return loginHistoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewLoginTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewLoginTime = itemView.findViewById(R.id.textViewLoginTime);
        }
    }
}