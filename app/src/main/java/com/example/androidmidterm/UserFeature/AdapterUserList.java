package com.example.androidmidterm.UserFeature;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidmidterm.R;
import com.example.androidmidterm.UserFeature.LoginHistory.ShowLoginHistoryActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterUserList extends RecyclerView.Adapter<AdapterUserList.ViewHolder> {
    private static final String TAG = "AdapterUserList";

    Context mContext;
    ArrayList<UserModel> mUserList;
    ActivityResultLauncher<Intent> launcherUpdateUser;
    AlertDialog progressDialog;

    public AdapterUserList() {
    }

    public AdapterUserList(Context mContext, ArrayList<UserModel> mUserList, ActivityResultLauncher<Intent> launcherUpdateUser) {
        this.mContext = mContext;
        this.mUserList = mUserList;
        this.launcherUpdateUser = launcherUpdateUser;
        progressDialog = new AlertDialog.Builder(mContext)
                .setView(R.layout.progress_layout)
                .setCancelable(false)
                .create();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(mContext).load(mUserList.get(position).getUserImage()).into(holder.userImage);
        holder.tvUserName.setText(mUserList.get(position).getUserName());
        holder.tvPhoneNumber.setText(mUserList.get(position).getUserPhone());
        holder.tvRole.setText(mUserList.get(position).getUserRole().toString());
        holder.swActive.setChecked(mUserList.get(position).isUserStatus());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popup = new PopupMenu(mContext, holder.itemView);
                popup.inflate(R.menu.menu_user_item);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        if (id == R.id.action_delete_user) {
                            new AlertDialog.Builder(mContext)
                                    .setTitle("Delete User")
                                    .setMessage("Are you sure you want to delete this user?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            progressDialog.show();
                                            FirebaseFirestore.getInstance().collection("Users")
                                                    .document(mUserList.get(position).getUserID())
                                                    .delete()
                                                    .addOnSuccessListener(aVoid -> {
                                                        mUserList.remove(position);
                                                        notifyDataSetChanged();
                                                        progressDialog.dismiss();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        progressDialog.dismiss();
                                                        Log.e(TAG, "onMenuItemClick: " + e.getMessage());
                                                    });
                                        }
                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                            notifyDataSetChanged();
                        } else if (id == R.id.action_edit_user) {
                            launcherUpdateUser.launch(new Intent(mContext, UpdateUserActivity.class)
                                    .putExtra("userPos", position)
                                    .putExtra("userID", mUserList.get(position).getUserID())
                                    .putExtra("userName", mUserList.get(position).getUserName())
                                    .putExtra("userAge", mUserList.get(position).getUserAge())
                                    .putExtra("userPhone", mUserList.get(position).getUserPhone())
                                    .putExtra("userRole", mUserList.get(position).getUserRole().toString())
                                    .putExtra("userStatus", mUserList.get(position).isUserStatus())
                                    .putExtra("userImage", mUserList.get(position).getUserImage()));
                        }
                        return true;
                    }
                });
                popup.show();
                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ShowLoginHistoryActivity.class);
                intent.putExtra("userID", mUserList.get(position).getUserID());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView userImage;
        TextView tvUserName, tvPhoneNumber, tvRole;
        Switch swActive;

        public ViewHolder(View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.userImage);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
            tvRole = itemView.findViewById(R.id.tvRole);
            swActive = itemView.findViewById(R.id.swActive);

        }
    }
}
