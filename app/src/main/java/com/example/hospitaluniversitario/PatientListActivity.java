package com.example.hospitaluniversitario;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.hospitaluniversitario.adapters.PatientAdapter;
import com.example.hospitaluniversitario.models.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientListActivity extends AppCompatActivity {

    private static final String TAG = "PatientListActivity";

    private RecyclerView recyclerView;
    private PatientAdapter patientAdapter;
    private List<Patient> patientList;
    private ProgressBar progressBar;
    private TextView tvNoPatientsFound;
    private FloatingActionButton fabAddPatient;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private ListenerRegistration patientListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_list);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {

            Toast.makeText(this, R.string.error_user_not_logged_in, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PatientListActivity.this, LoginActivity.class));
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerViewPatients);
        progressBar = findViewById(R.id.progressBarPatientList);
        tvNoPatientsFound = findViewById(R.id.tvNoPatientsFound);
        fabAddPatient = findViewById(R.id.fabAddPatient);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        patientList = new ArrayList<>();
        patientAdapter = new PatientAdapter(patientList);
        recyclerView.setAdapter(patientAdapter);

        fabAddPatient.setOnClickListener(v -> {
            Intent intent = new Intent(PatientListActivity.this, SelectPatientUserActivity.class);
            startActivity(intent);
        });

        patientAdapter.setOnItemClickListener(patient -> {
            Intent intent = new Intent(PatientListActivity.this, AddEditPatientActivity.class);
            intent.putExtra("patient_uid", patient.getId());
            startActivity(intent);
        });
        loadPatients();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPatients();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (patientListener != null) {
            patientListener.remove();
            patientListener = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (patientListener != null) {
            patientListener.remove();
        }
    }
    private void loadPatients() {
        showProgressBar();

        String appId = getApplicationContext().getPackageName();
        CollectionReference patientsRef = db.collection("artifacts").document(appId)
                .collection("public").document("data")
                .collection("patients");

        Query query = patientsRef;
        if (patientListener != null) {
            patientListener.remove();
        }

        patientListener = query.addSnapshotListener((snapshots, e) -> {
            hideProgressBar();
            if (e != null) {
                Log.w(TAG, "Error al escuchar cambios en pacientes: ", e);
                tvNoPatientsFound.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);

                return;
            }

            List<Patient> fetchedPatients = new ArrayList<>();
            if (snapshots != null) {
                for (QueryDocumentSnapshot doc : snapshots) {
                    try {
                        Patient patient = doc.toObject(Patient.class);
                        if (patient != null) {
                            patient.setId(doc.getId());
                            fetchedPatients.add(patient);
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "Error al convertir documento a objeto Patient: " + doc.getId(), ex);

                    }
                }
            }

            patientList.clear();
            patientList.addAll(fetchedPatients);
            patientAdapter.updatePatients(fetchedPatients);

            if (fetchedPatients.isEmpty()) {
                tvNoPatientsFound.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvNoPatientsFound.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }
}
