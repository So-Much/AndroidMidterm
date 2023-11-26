package com.example.androidmidterm.StudentFeature;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    FloatingActionButton fabScanStudentCSV;
    SearchView searchView;
    Spinner spinnerSort;
    ArrayList<StudentModel> students = new ArrayList<>();
    AdapterStudentList adapterStudentList;
    RecyclerView rvStudentList;
    private ListenerRegistration listenerRegistration;
    View view;
    private String[] sortOptions = {"Select Once","Student Name (A-Z)", "Student Name (Z-A)", "Student GPA(low to high)", "Student GPA(high to low)"};

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
        fabScanStudentCSV = view.findViewById(R.id.fabScanStudentCSV);
        rvStudentList = view.findViewById(R.id.rvStudentList);
        searchView = view.findViewById(R.id.searchView);
        spinnerSort = view.findViewById(R.id.spinnerSort);

        rvStudentList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        adapterStudentList = new AdapterStudentList(view.getContext(), students);


        fabAddStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), PostStudentActivity.class);
                intent.setData(null);
                startActivity(intent);
            }
        });
        fabScanStudentCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                searchForMultipleProperties(newText);
                return false;
            }
        });

        ArrayAdapter<CharSequence> sortAdapter = new ArrayAdapter<CharSequence>(view.getContext(), android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

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

    private void searchForMultipleProperties(String newText) {
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

}