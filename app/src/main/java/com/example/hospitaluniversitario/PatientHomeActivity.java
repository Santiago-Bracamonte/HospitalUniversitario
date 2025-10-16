package com.example.hospitaluniversitario;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.hospitaluniversitario.models.Patient;
import com.example.hospitaluniversitario.models.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PatientHomeActivity extends AppCompatActivity {

    private static final String TAG = "PatientHomeActivity";

    private TextView tvWelcomePatient, tvMyDiagnosis, tvMyDoctor, tvNextAppointment;
    private Button btnAddToCalendar, btnLogout;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private Patient currentPatientData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        tvWelcomePatient = findViewById(R.id.tvWelcomePatient);
        tvMyDiagnosis = findViewById(R.id.tvMyDiagnosis);
        tvMyDoctor = findViewById(R.id.tvMyDoctor);
        tvNextAppointment = findViewById(R.id.tvNextAppointment);
        btnAddToCalendar = findViewById(R.id.btnAddToCalendar);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBarPatientHome);

        btnLogout.setOnClickListener(v -> logoutUser());
        btnAddToCalendar.setOnClickListener(v -> addAppointmentToCalendar());

        if (currentUser != null) {
            loadPatientProfileAndData(currentUser.getUid());
        } else {
            startActivity(new Intent(PatientHomeActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void loadPatientProfileAndData(String uid) {
        showProgressBar();
        String appId = getPackageName();

        DocumentReference userProfileRef = db.collection("artifacts").document(appId)
                .collection("users").document(uid)
                .collection("user_profiles").document(uid);

        userProfileRef.get().addOnSuccessListener(userSnapshot -> {
            if (userSnapshot.exists()) {
                User userProfile = userSnapshot.toObject(User.class);
                if (userProfile != null) {
                    String userName = userProfile.getName() != null && !userProfile.getName().isEmpty() ? userProfile.getName() : currentUser.getEmail();
                    tvWelcomePatient.setText(getString(R.string.welcome_message, userName, getString(R.string.patient_role)));
                }
            } else {
                Log.w(TAG, "Perfil de usuario no encontrado para UID: " + uid);
                tvWelcomePatient.setText(getString(R.string.welcome_message, currentUser.getEmail(), getString(R.string.patient_role)));
            }

            DocumentReference patientDocRef = db.collection("artifacts").document(appId)
                    .collection("public").document("data")
                    .collection("patients").document(uid);

            patientDocRef.get().addOnSuccessListener(patientSnapshot -> {
                hideProgressBar();
                if (patientSnapshot.exists()) {
                    currentPatientData = patientSnapshot.toObject(Patient.class);
                    if (currentPatientData != null) {
                        currentPatientData.setId(patientSnapshot.getId());

                        tvMyDiagnosis.setText(getString(R.string.label_my_diagnosis) + currentPatientData.getDiagnosis());
                        tvMyDoctor.setText(getString(R.string.label_my_doctor) + currentPatientData.getAttendingDoctor());

                        if (currentPatientData.getVisitDate() != null && !currentPatientData.getVisitDate().isEmpty()) {
                            tvNextAppointment.setText(getString(R.string.label_next_appointment) + currentPatientData.getVisitDate());
                        } else {
                            tvNextAppointment.setText(getString(R.string.label_next_appointment) + "N/A");
                        }
                        btnAddToCalendar.setVisibility(View.VISIBLE);
                    } else {
                        tvMyDiagnosis.setText(getString(R.string.label_my_diagnosis) + "Aún no hay diagnostico.");
                        tvMyDoctor.setText(getString(R.string.label_my_doctor) + "N/A");
                        tvNextAppointment.setText(getString(R.string.label_next_appointment) + "N/A");
                        btnAddToCalendar.setVisibility(View.GONE);
                    }
                } else {
                    hideProgressBar();
                    tvMyDiagnosis.setText(getString(R.string.label_my_diagnosis) + "Aún no hay diagnostico.");
                    tvMyDoctor.setText(getString(R.string.label_my_doctor) + "N/A");
                    tvNextAppointment.setText(getString(R.string.label_next_appointment) + "N/A");
                    btnAddToCalendar.setVisibility(View.GONE);
                    Toast.makeText(PatientHomeActivity.this, "No patient record found for this user.", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(e -> {
                hideProgressBar();
                tvMyDiagnosis.setText(getString(R.string.label_my_diagnosis) + "Error loading data.");
                tvMyDoctor.setText(getString(R.string.label_my_doctor) + "Error loading data.");
                tvNextAppointment.setText(getString(R.string.label_next_appointment) + "Error loading data.");
                btnAddToCalendar.setVisibility(View.GONE);
                Toast.makeText(PatientHomeActivity.this, "Error al cargar datos del paciente: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }).addOnFailureListener(e -> {
            hideProgressBar();
            Toast.makeText(PatientHomeActivity.this, "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
            mAuth.signOut();
            startActivity(new Intent(PatientHomeActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void addAppointmentToCalendar() {
        if (currentPatientData == null || currentPatientData.getVisitDate() == null || currentPatientData.getVisitDate().isEmpty()) {
            Toast.makeText(this, R.string.error_no_appointment_date, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date appointmentDate = formatter.parse(currentPatientData.getVisitDate());

            Calendar beginTime = Calendar.getInstance();
            beginTime.setTime(appointmentDate);
            beginTime.set(Calendar.HOUR_OF_DAY, 9);
            beginTime.set(Calendar.MINUTE, 0);

            Calendar endTime = Calendar.getInstance();
            endTime.setTime(appointmentDate);
            endTime.set(Calendar.HOUR_OF_DAY, 10);
            endTime.set(Calendar.MINUTE, 0);

            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(android.provider.CalendarContract.Events.CONTENT_URI)
                    .putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                    .putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                    .putExtra(android.provider.CalendarContract.Events.TITLE, getString(R.string.calendar_event_title, currentPatientData.getAttendingDoctor()))
                    .putExtra(android.provider.CalendarContract.Events.DESCRIPTION, getString(R.string.calendar_event_description, currentPatientData.getDiagnosis()))
                    .putExtra(android.provider.CalendarContract.Events.EVENT_LOCATION, currentPatientData.getHospitalLocationAddress())
                    .putExtra(android.provider.CalendarContract.Events.ALL_DAY, true);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                Toast.makeText(this, R.string.success_calendar_add, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.error_calendar_add_failed, Toast.LENGTH_LONG).show();
            }

        } catch (ParseException e) {
            Toast.makeText(this, "Error with visit date format.", Toast.LENGTH_SHORT).show();
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Sesión cerrada.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(PatientHomeActivity.this, LoginActivity.class));
        finish();
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
