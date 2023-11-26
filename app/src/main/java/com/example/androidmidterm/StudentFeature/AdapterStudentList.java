package com.example.androidmidterm.StudentFeature;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidmidterm.R;
import com.example.androidmidterm.UserFeature.LoginHistory.ShowLoginHistoryActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdapterStudentList extends RecyclerView.Adapter<AdapterStudentList.ViewHolder> {
    Context mContext;
    ArrayList<StudentModel> mStudentList;

    public AdapterStudentList() {
    }

    public AdapterStudentList(Context mContext, ArrayList<StudentModel> mStudentList) {
        this.mContext = mContext;
        this.mStudentList = mStudentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_student, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvStudentName.setText(mStudentList.get(position).getStudentName());
        holder.tvStudentNumber.setText(mStudentList.get(position).getStudentNumber());
        holder.tvStudentGPA.setText("GPA: "+mStudentList.get(position).getStudentGPA());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                detail student and certificate
                Intent intent = new Intent(mContext, StudentDetailActivity.class);
                intent.putExtra("studentName", mStudentList.get(position).getStudentName());
                intent.putExtra("studentNumber", mStudentList.get(position).getStudentNumber());
                intent.putExtra("studentGPA", mStudentList.get(position).getStudentGPA());
                mContext.startActivity(intent);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
//                menu to update or delete student
                PopupMenu popup = new PopupMenu(mContext, holder.itemView);
                popup.inflate(R.menu.menu_student_item);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        if (id == R.id.action_delete_student) {
                            new AlertDialog.Builder(mContext)
                                    .setTitle("Delete Student")
                                    .setMessage("Are you sure you want to delete this student?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            FirebaseFirestore.getInstance()
                                                    .collection("Students")
                                                    .document(mStudentList.get(position).getStudentNumber())
                                                    .delete()
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d("TAG", "onSuccess: Student deleted");
                                                        Toast.makeText(mContext, "Student deleted", Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        } else if (id == R.id.action_edit_student) {
                            Intent intent = new Intent(mContext, PostStudentActivity.class);
                            intent.setData(new Uri.Builder().appendPath(mStudentList.get(position).getStudentNumber()).build());
                            intent.putExtra("studentName", mStudentList.get(position).getStudentName());
                            intent.putExtra("studentNumber", mStudentList.get(position).getStudentNumber());
                            intent.putExtra("studentGPA", mStudentList.get(position).getStudentGPA());
                            mContext.startActivity(intent);
                        }
                        return true;
                    }
                });
                popup.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mStudentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStudentNumber, tvStudentGPA;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentNumber = itemView.findViewById(R.id.tvStudentNumber);
            tvStudentGPA = itemView.findViewById(R.id.tvStudentGPA);
        }
    }
    public void filterList(ArrayList<StudentModel> filteredList) {
        notifyDataSetChanged();
        mStudentList = filteredList;
    }

    public void sortByName(String type) {
        if (type.equals("ASC")) {
            mStudentList.sort((o1, o2) -> o1.getStudentName().compareTo(o2.getStudentName()));
        } else {
            mStudentList.sort((o1, o2) -> o2.getStudentName().compareTo(o1.getStudentName()));
        }
        notifyDataSetChanged();
    }
    public void sortByGPA(String type) {
        if (type.equals("ASC")) {
            mStudentList.sort((o1, o2) -> o1.getStudentGPA().compareTo(o2.getStudentGPA()));
        } else {
            mStudentList.sort((o1, o2) -> o2.getStudentGPA().compareTo(o1.getStudentGPA()));
        }
        notifyDataSetChanged();
    }
}
