package com.example.androidmidterm.StudentFeature.Certificate;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidmidterm.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdapterCertificateList extends RecyclerView.Adapter<AdapterCertificateList.ViewHolder>{
    Context mContext;
    ArrayList<CertificateModel> certificates;
    public AdapterCertificateList() {
    }

    public AdapterCertificateList(Context mContext, ArrayList<CertificateModel> certificates) {
        this.mContext = mContext;
        this.certificates = certificates;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_certificate, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvCertificateName.setText(certificates.get(position).getCertificateName());
        holder.tvCertificateDateIssued.setText("Issued at: " +certificates.get(position).getCertificateDateIssued());
        holder.tvCertificateDateExpired.setText("Expired at: " +certificates.get(position).getCertificateDateExpired());

        holder.itemView.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(mContext, v);
            popupMenu.inflate(R.menu.menu_certificate_item);
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_edit_certificate){
//                    edit certificate
                    View viewEditCertificate = LayoutInflater.from(mContext).inflate(R.layout.dialog_add_certificate, null);
                    TextView tvTitle = viewEditCertificate.findViewById(R.id.tvTitle);
                    tvTitle.setText("Edit Certificate");
                    EditText etCertificateName = viewEditCertificate.findViewById(R.id.etCertificateName);
                    EditText etIssuedDay = viewEditCertificate.findViewById(R.id.etIssuedDay);
                    EditText etIssuedMonth = viewEditCertificate.findViewById(R.id.etIssuedMonth);
                    EditText etIssuedYear = viewEditCertificate.findViewById(R.id.etIssuedYear);
                    EditText etExpiredDay = viewEditCertificate.findViewById(R.id.etExpiredDay);
                    EditText etExpiredMonth = viewEditCertificate.findViewById(R.id.etExpiredMonth);
                    EditText etExpiredYear = viewEditCertificate.findViewById(R.id.etExpiredYear);

                    String dateIssued[] = certificates.get(position).getCertificateDateIssued().split("/");
                    String dateExpired[] = certificates.get(position).getCertificateDateExpired().split("/");

                    etCertificateName.setText(certificates.get(position).getCertificateName());
                    etIssuedDay.setText(dateIssued[0]);
                    etIssuedMonth.setText(dateIssued[1]);
                    etIssuedYear.setText(dateIssued[2]);
                    etExpiredDay.setText(dateExpired[0]);
                    etExpiredMonth.setText(dateExpired[1]);
                    etExpiredYear.setText(dateExpired[2]);

                    new AlertDialog.Builder(mContext)
                            .setView(viewEditCertificate)
                            .setPositiveButton("Yes", (dialog, which) -> {
//                                edit certificate
                                String certificateName = etCertificateName.getText().toString();
                                String certificateDateIssued = etIssuedDay.getText().toString() + "/" + etIssuedMonth.getText().toString() + "/" + etIssuedYear.getText().toString();
                                String certificateDateExpired = etExpiredDay.getText().toString() + "/" + etExpiredMonth.getText().toString() + "/" + etExpiredYear.getText().toString();

                                String StudentID = certificates.get(position).getStudentID();
                                String CertificateID = certificates.get(position).getCertificateID();

                                FirebaseFirestore.getInstance()
                                        .collection("Students")
                                        .document(StudentID)
                                        .collection("Certificates")
                                        .document(CertificateID)
                                        .update("certificateName", certificateName,
                                                "certificateDateIssued", certificateDateIssued,
                                                "certificateDateExpired", certificateDateExpired)
                                        .addOnSuccessListener(aVoid -> {
                                            certificates.get(position).setCertificateName(certificateName);
                                            certificates.get(position).setCertificateDateIssued(certificateDateIssued);
                                            certificates.get(position).setCertificateDateExpired(certificateDateExpired);
                                            notifyItemChanged(position);
                                            Toast.makeText(mContext, "Certificate is Updated!", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .setNegativeButton("No", null)
                            .show();
                    return true;
                } else if (id == R.id.menu_delete_certificate){
//                    delete certificate
                    new AlertDialog.Builder(mContext)
                            .setTitle("Delete Certificate")
                            .setMessage("Are you sure you want to delete this certificate?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                FirebaseFirestore.getInstance()
                                        .collection("Students")
                                        .document(certificates.get(position).getStudentID())
                                        .collection("Certificates")
                                        .document(certificates.get(position).getCertificateID())
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(mContext, "Certificate deleted", Toast.LENGTH_SHORT).show();
                                        });
                                certificates.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, certificates.size());
                            })
                            .setNegativeButton("No", null)
                            .show();
                    return true;
                } else {
                    return false;
                }
            });
            popupMenu.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return certificates.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvCertificateName, tvCertificateDateIssued, tvCertificateDateExpired;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCertificateName = itemView.findViewById(R.id.tvCertificateName);
            tvCertificateDateIssued = itemView.findViewById(R.id.tvDateIssued);
            tvCertificateDateExpired = itemView.findViewById(R.id.tvDateExpired);
        }
    }
}
