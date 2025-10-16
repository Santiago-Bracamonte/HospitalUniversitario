package com.example.hospitaluniversitario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hospitaluniversitario.models.Patient;
import com.google.firebase.firestore.FirebaseFirestore;

public class PatientDetailActivity extends AppCompatActivity {

    private TextView tvPatientDetailTitle;
    private TextView tvPatientName, tvPatientAge, tvPatientGender, tvPatientDiagnosis,
            tvPatientRoomNumber, tvPatientVisitDate, tvPatientAttendingDoctor,
            tvHospitalAddress;
    private Button btnEditPatient, btnDeletePatient;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private String patientId;
    private Patient currentPatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);

        db = FirebaseFirestore.getInstance();

        tvPatientDetailTitle = findViewById(R.id.tvPatientDetailTitle);
        tvPatientName = findViewById(R.id.tvPatientName);
        tvPatientAge = findViewById(R.id.tvPatientAge);
        tvPatientGender = findViewById(R.id.tvPatientGender);
        tvPatientDiagnosis = findViewById(R.id.tvPatientDiagnosis);
        tvPatientRoomNumber = findViewById(R.id.tvPatientRoomNumber);
        tvPatientVisitDate = findViewById(R.id.tvPatientVisitDate);
        tvPatientAttendingDoctor = findViewById(R.id.tvPatientAttendingDoctor);
        tvHospitalAddress = findViewById(R.id.tvHospitalAddress);
        btnEditPatient = findViewById(R.id.btnEditPatient);
        btnDeletePatient = findViewById(R.id.btnDeletePatient);
        progressBar = findViewById(R.id.progressBarDetail);

        if (getIntent().hasExtra("patient_id")) {
            patientId = getIntent().getStringExtra("patient_id");
            loadPatientData(patientId);
        } else {
            Toast.makeText(this, "Error: No se proporcionó ID de paciente.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnEditPatient.setOnClickListener(v -> {
            if (patientId != null) {
                Intent intent = new Intent(PatientDetailActivity.this, AddEditPatientActivity.class);
                intent.putExtra("patient_uid", patientId);
                startActivity(intent);
            }
        });

        btnDeletePatient.setOnClickListener(v -> {
            if (patientId != null) {
                showDeleteConfirmationDialog(patientId);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (patientId != null) {
            loadPatientData(patientId);
        }
    }


    private void loadPatientData(String uid) {
        showProgressBar();
        String appId = getPackageName();

        db.collection("artifacts").document(appId)
                .collection("public").document("data")
                .collection("patients").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    hideProgressBar();
                    if (documentSnapshot.exists()) {
                        currentPatient = documentSnapshot.toObject(Patient.class);
                        if (currentPatient != null) {
                            currentPatient.setId(documentSnapshot.getId());

                            tvPatientName.setText(currentPatient.getName());
                            tvPatientAge.setText(currentPatient.getAge());
                            tvPatientGender.setText(currentPatient.getGender());
                            tvPatientDiagnosis.setText(currentPatient.getDiagnosis());
                            tvPatientRoomNumber.setText(currentPatient.getRoomNumber());
                            tvPatientVisitDate.setText(currentPatient.getVisitDate());
                            tvPatientAttendingDoctor.setText(currentPatient.getAttendingDoctor());
                            tvHospitalAddress.setText(currentPatient.getHospitalLocationAddress());
                        }
                    } else {
                        Toast.makeText(PatientDetailActivity.this, "Paciente no encontrado.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    hideProgressBar();
                    finish();
                });
    }


    private void showDeleteConfirmationDialog(String id) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_patient_title)
                .setMessage(getString(R.string.confirm_delete_patient_message, currentPatient != null ? currentPatient.getName() : "este paciente"))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deletePatient(id))
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void deletePatient(String uid) {
        showProgressBar();
        String appId = getPackageName();

        db.collection("artifacts").document(appId)
                .collection("public").document("data")
                .collection("patients").document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    hideProgressBar();
                    Toast.makeText(PatientDetailActivity.this, R.string.success_patient_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    hideProgressBar();
                    Toast.makeText(PatientDetailActivity.this, getString(R.string.error_deleting_patient) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
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
