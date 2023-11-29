package com.example.androidmidterm.StudentFeature;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidmidterm.FileManagement.ExportCSV;
import com.example.androidmidterm.FileManagement.ImportCSV;
import com.example.androidmidterm.R;
import com.example.androidmidterm.UserFeature.UserModel;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class StudentFragment extends Fragment {
    private static final String TAG = "StudentFragment";
    UserModel currentUser;
    FloatingActionButton fabAddStudent;
    FloatingActionButton fabImportStudentCSV, fabExportStudentCSV;
    AppCompatButton btnMultipleSearch;
    SearchView searchView;
    Spinner spinnerSort;
    ArrayList<StudentModel> students = new ArrayList<>();
    AdapterStudentList adapterStudentList;
    RecyclerView rvStudentList;
    ImportCSV importCSV;
    ExportCSV exportCSV;
    ActivityResultLauncher<Intent> filePickerLauncher, folderPickerLauncher;
    private ListenerRegistration listenerRegistration;
    View view;
    private String[] sortOptions = {"Select Once", "Student Name (A-Z)", "Student Name (Z-A)", "Student GPA(low to high)", "Student GPA(high to low)"};

    public StudentFragment() {
        // Required empty public constructor
    }

    public StudentFragment(UserModel currentUser) {
        this.currentUser = currentUser;
    }

    public static StudentFragment newInstance(UserModel currentUser) {
        StudentFragment fragment = new StudentFragment(currentUser);
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
        view = inflater.inflate(R.layout.fragment_student, container, false);

        fabAddStudent = view.findViewById(R.id.fabAddStudent);
        fabImportStudentCSV = view.findViewById(R.id.fabImportStudentCSV);
        fabExportStudentCSV = view.findViewById(R.id.fabExportStudentCSV);
        rvStudentList = view.findViewById(R.id.rvStudentList);
        searchView = view.findViewById(R.id.searchView);
        spinnerSort = view.findViewById(R.id.spinnerSort);
        btnMultipleSearch = view.findViewById(R.id.btnMultipleSearch);
        exportCSV = new ExportCSV(view.getContext());
        importCSV = new ImportCSV(view.getContext());


        rvStudentList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        adapterStudentList = new AdapterStudentList(view.getContext(), students);

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                resultFile -> {
                    if (resultFile.getResultCode() == getActivity().RESULT_OK) {
                        Uri src = resultFile.getData().getData();
                        importCSV.importStudents(src, new ImportCSV.ImportStudentListener() {
                            @Override
                            public void onImportStudentSuccess(ArrayList<StudentModel> studentImported) {
                                students.addAll(studentImported);
                                adapterStudentList.notifyDataSetChanged();
                                Toast.makeText(view.getContext(), "Import CSV Success", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onImportStudentFailure() {
                                Toast.makeText(view.getContext(), "Import CSV Failure", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        );
        folderPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK) {
                        Uri src = result.getData().getData();
                        EditText editText = new EditText(view.getContext());
                        new AlertDialog.Builder(view.getContext())
                                .setTitle("Export CSV")
                                .setMessage("File name:")
                                .setView(editText)
                                .setPositiveButton("Export", (dialogInterface, i) -> {
                                    String filename = editText.getText().toString().trim();
                                    if (filename.isEmpty()) {
                                        editText.setError("Please enter filename");
                                    } else {
                                        editText.setError(null);
                                        if (!filename.endsWith(".csv")) {
                                            filename += ".csv";
                                        }
                                        DocumentFile pickedDir = DocumentFile.fromTreeUri(view.getContext(), src);
                                        DocumentFile newFile = pickedDir.createFile("text/csv", filename);
                                        exportCSV.exportStudents(students, newFile, new ExportCSV.ExportCSVListener() {
                                            @Override
                                            public void onExportCSVSuccess() {
                                                Toast.makeText(view.getContext(), "Export CSV Success", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onExportCSVFailure() {
                                                Toast.makeText(view.getContext(), "Export CSV Failure", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                }
        );


        fabAddStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), PostStudentActivity.class);
                intent.setData(null);
                startActivity(intent);
            }
        });
//        Import
        fabImportStudentCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent filePicker = new Intent(Intent.ACTION_GET_CONTENT);
                filePicker.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                filePicker.setType("*/*");
                filePicker = Intent.createChooser(filePicker, "Select a file");
                filePickerLauncher.launch(filePicker);
            }
        });
//        Export
        fabExportStudentCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Intent folderPicker = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    folderPicker.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    folderPicker.addCategory(Intent.CATEGORY_DEFAULT);
                    folderPickerLauncher.launch(Intent.createChooser(folderPicker, "Select a folder"));
                }
            }
        });
        listenerRegistration = FirebaseFirestore.getInstance()
                .collection("Students")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(view.getContext(), "Listen failed.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "onEvent: " + error.getMessage());
                        }
                        students.clear();
                        if (value != null && !value.isEmpty()) {
                            for (int i = 0; i < value.getDocuments().size(); i++) {
                                StudentModel studentModel = value.getDocuments().get(i).toObject(StudentModel.class);
                                students.add(studentModel);
                            }
                        }
                        adapterStudentList.notifyDataSetChanged();
                    }
                });
        rvStudentList.setAdapter(adapterStudentList);

        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return false;
            }
        });


        ArrayAdapter<CharSequence> sortAdapter = new ArrayAdapter<CharSequence>(view.getContext(), android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);
        btnMultipleSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Multiple Search")
                        .setView(R.layout.multiple_search_view)
                        .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                EditText etStudentNumber = ((AlertDialog) dialogInterface).findViewById(R.id.etStudentNumber);
                                EditText etStudentName = ((AlertDialog) dialogInterface).findViewById(R.id.etStudentName);
                                EditText etFromGPA = ((AlertDialog) dialogInterface).findViewById(R.id.etFromGPA);
                                EditText etToGPA = ((AlertDialog) dialogInterface).findViewById(R.id.etToGPA);

                                String studentNumber = etStudentNumber.getText().toString().trim().isEmpty() ? "" : etStudentNumber.getText().toString().trim();
                                String studentName = etStudentName.getText().toString().trim().isEmpty() ? "" : etStudentName.getText().toString().trim();
                                double fromGPA = 0;
                                double toGPA = 0;
                                try {
                                    fromGPA = etFromGPA.getText().toString().trim().isEmpty() ? 0 : Double.parseDouble(etFromGPA.getText().toString().trim());
                                    toGPA = etToGPA.getText().toString().trim().isEmpty() ? 10 : Double.parseDouble(etToGPA.getText().toString().trim());
                                } catch (Exception e) {
                                    Toast.makeText(view.getContext(), "Please enter valid GPA", Toast.LENGTH_SHORT).show();
                                }
                                searchMultiple(studentNumber, studentName, fromGPA, toGPA);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    switch (i) {
                        case 1:
                            adapterStudentList.sortByName("ASC");
                            break;
                        case 2:
                            adapterStudentList.sortByName("DESC");
                            break;
                        case 3:
                            adapterStudentList.sortByGPA("ASC");
                            break;
                        case 4:
                            adapterStudentList.sortByGPA("DESC");
                            break;
                    }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        return view;
    }

    private void search(String newText) {
        ArrayList<StudentModel> filteredList = new ArrayList<>();
        for (StudentModel student : students) {
            if (student.getStudentName().toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(student);
            }else {
                if (student.getStudentNumber().contains(newText.trim())) {
                    filteredList.add(student);
                }
            }
        }
        adapterStudentList.filterList(filteredList);
    }
    public void searchMultiple(String studentNumber, String studentName, double fromGPA, double toGPA) {
        ArrayList<StudentModel> filteredList = new ArrayList<>();
        for (StudentModel student : students) {
            if (student.getStudentNumber().contains(studentNumber) && student.getStudentName().toLowerCase().contains(studentName.toLowerCase()) && student.getStudentGPA() >= fromGPA && student.getStudentGPA() <= toGPA) {
                filteredList.add(student);
            }
        }
        adapterStudentList.filterList(filteredList);
    }
}