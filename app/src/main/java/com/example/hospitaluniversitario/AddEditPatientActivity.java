package com.example.hospitaluniversitario;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.hospitaluniversitario.models.Patient;
import com.example.hospitaluniversitario.utils.ValidationUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditPatientActivity extends AppCompatActivity {

    private static final String TAG = "AddEditPatientActivity";

    private EditText etPatientName, etPatientAge, etPatientDiagnosis,
            etPatientRoomNumber, etPatientVisitDate, etPatientAttendingDoctor,
            etHospitalAddress, etPatientOtherGender;
    private Spinner spinnerPatientGender;
    private Button btnSavePatient, btnCancel, btnAddToCalendar, btnDeletePatient;
    private TextView tvAddEditPatientTitle;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private String patientUid;
    private Patient currentPatient;

    private SimpleDateFormat dateFormatter;
    private String selectedGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_patient);

        db = FirebaseFirestore.getInstance();
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        tvAddEditPatientTitle = findViewById(R.id.tvAddEditPatientTitle);
        etPatientName = findViewById(R.id.etPatientName);
        etPatientAge = findViewById(R.id.etPatientAge);
        spinnerPatientGender = findViewById(R.id.spinnerPatientGender);
        etPatientOtherGender = findViewById(R.id.etPatientOtherGender);
        etPatientDiagnosis = findViewById(R.id.etPatientDiagnosis);
        etPatientRoomNumber = findViewById(R.id.etPatientRoomNumber);
        etPatientVisitDate = findViewById(R.id.etPatientVisitDate);
        etPatientAttendingDoctor = findViewById(R.id.etPatientAttendingDoctor);
        etHospitalAddress = findViewById(R.id.etHospitalAddress);
        btnSavePatient = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnAddToCalendar = findViewById(R.id.btnAddToCalendar);
        btnDeletePatient = findViewById(R.id.btnDeletePatient);
        progressBar = findViewById(R.id.progressBarAddEdit);

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_options,
                android.R.layout.simple_spinner_item
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPatientGender.setAdapter(genderAdapter);

        spinnerPatientGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGender = parent.getItemAtPosition(position).toString();
                if (selectedGender.equals("Otro")) {
                    etPatientOtherGender.setVisibility(View.VISIBLE);
                    etPatientOtherGender.requestFocus();
                } else {
                    etPatientOtherGender.setVisibility(View.GONE);
                    etPatientOtherGender.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (getIntent().hasExtra("patient_uid")) {
            patientUid = getIntent().getStringExtra("patient_uid");
            tvAddEditPatientTitle.setText(R.string.edit_patient_title);
            loadPatientData(patientUid);
        } else {
            Toast.makeText(this, "Error: No se proporcionó UID de paciente.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etPatientVisitDate.setOnClickListener(v -> showDatePickerDialog());
        etPatientVisitDate.setFocusable(false);
        etPatientVisitDate.setKeyListener(null);

        btnSavePatient.setOnClickListener(v -> savePatient());
        btnCancel.setOnClickListener(v -> finish());
        btnAddToCalendar.setOnClickListener(v -> addAppointmentToCalendar());
        btnDeletePatient.setOnClickListener(v -> showDeleteConfirmationDialog());
    }


    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.DatePickerDialogTheme,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    etPatientVisitDate.setText(dateFormatter.format(selectedDate.getTime()));
                },
                year, month, day);


        datePickerDialog.show();
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

                            etPatientName.setText(currentPatient.getName());
                            etPatientAge.setText(currentPatient.getAge());

                            String gender = currentPatient.getGender();
                            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerPatientGender.getAdapter();
                            int spinnerPosition = adapter.getPosition(gender);

                            if (spinnerPosition >= 0) {
                                spinnerPatientGender.setSelection(spinnerPosition);
                            } else {
                                spinnerPatientGender.setSelection(adapter.getPosition("Otro"));
                                etPatientOtherGender.setVisibility(View.VISIBLE);
                                etPatientOtherGender.setText(gender);
                            }

                            etPatientDiagnosis.setText(currentPatient.getDiagnosis());
                            etPatientRoomNumber.setText(currentPatient.getRoomNumber());
                            etPatientVisitDate.setText(currentPatient.getVisitDate());
                            etPatientAttendingDoctor.setText(currentPatient.getAttendingDoctor());
                            etHospitalAddress.setText(currentPatient.getHospitalLocationAddress());

                            etPatientName.setEnabled(false);
                            etPatientAge.setEnabled(false);

                            btnAddToCalendar.setVisibility(View.VISIBLE);
                            btnDeletePatient.setVisibility(View.VISIBLE);
                        }
                    } else {

                        Toast.makeText(AddEditPatientActivity.this, "Creando nuevo historial para el paciente seleccionado.", Toast.LENGTH_SHORT).show();
                        tvAddEditPatientTitle.setText(R.string.add_patient_title);

                        loadPatientUserProfile(uid);

                        btnAddToCalendar.setVisibility(View.GONE);
                        btnDeletePatient.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    hideProgressBar();
                    finish();
                });
    }


    private void loadPatientUserProfile(String uid) {
        String appId = getPackageName();
        db.collection("artifacts").document(appId)
                .collection("users").document(uid)
                .collection("user_profiles").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        com.example.hospitaluniversitario.models.User patientUser = documentSnapshot.toObject(com.example.hospitaluniversitario.models.User.class);
                        if (patientUser != null) {
                            etPatientName.setText(patientUser.getName() != null && !patientUser.getName().isEmpty() ? patientUser.getName() : patientUser.getEmail());
                            etPatientName.setEnabled(false);
                        }
                    } else {
                        Toast.makeText(AddEditPatientActivity.this, "No se encontró el perfil de usuario para este paciente.", Toast.LENGTH_SHORT).show();
                        etPatientName.setText("");
                        etPatientName.setEnabled(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddEditPatientActivity.this, "Error al cargar el perfil del usuario paciente: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    etPatientName.setText("");
                    etPatientName.setEnabled(true);
                });
    }


    private void savePatient() {
        String name = etPatientName.getText().toString().trim();
        String age = etPatientAge.getText().toString().trim();
        String diagnosis = etPatientDiagnosis.getText().toString().trim();
        String roomNumber = etPatientRoomNumber.getText().toString().trim();
        String visitDate = etPatientVisitDate.getText().toString().trim();
        String attendingDoctor = etPatientAttendingDoctor.getText().toString().trim();
        String hospitalAddress = etHospitalAddress.getText().toString().trim();

        String genderToSave = selectedGender;
        if (selectedGender.equals("Otro")) {
            genderToSave = etPatientOtherGender.getText().toString().trim();
            if (ValidationUtils.isEmpty(genderToSave)) {
                etPatientOtherGender.setError(getString(R.string.error_other_gender_empty));
                etPatientOtherGender.requestFocus();
                Toast.makeText(this, R.string.error_other_gender_empty, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (ValidationUtils.isEmpty(name) ||
                ValidationUtils.isEmpty(age) ||
                ValidationUtils.isEmpty(genderToSave) ||
                ValidationUtils.isEmpty(diagnosis) ||
                ValidationUtils.isEmpty(roomNumber) ||
                ValidationUtils.isEmpty(visitDate) ||
                ValidationUtils.isEmpty(hospitalAddress)) {
            Toast.makeText(this, R.string.error_empty_field, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidVisitDate(visitDate)) {
            etPatientVisitDate.setError(getString(R.string.error_invalid_visit_date));
            etPatientVisitDate.requestFocus();
            return;
        }

        showProgressBar();
        btnSavePatient.setEnabled(false);
        btnDeletePatient.setEnabled(false);

        if (currentPatient == null) {
            currentPatient = new Patient();
        }
        currentPatient.setId(patientUid);
        currentPatient.setName(name);
        currentPatient.setAge(age);
        currentPatient.setGender(genderToSave);
        currentPatient.setDiagnosis(diagnosis);
        currentPatient.setRoomNumber(roomNumber);
        currentPatient.setVisitDate(visitDate);
        currentPatient.setAttendingDoctor(attendingDoctor);
        currentPatient.setHospitalLocationAddress(hospitalAddress);

        String appId = getPackageName();
        CollectionReference patientsCollection = db.collection("artifacts").document(appId)
                .collection("public").document("data")
                .collection("patients");

        patientsCollection.document(patientUid).set(currentPatient)
                .addOnSuccessListener(aVoid -> {
                    hideProgressBar();
                    btnSavePatient.setEnabled(true);
                    btnDeletePatient.setEnabled(true);
                    Toast.makeText(AddEditPatientActivity.this, R.string.success_patient_updated, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    hideProgressBar();
                    btnSavePatient.setEnabled(true);
                    btnDeletePatient.setEnabled(true);
                    Log.e(TAG, "Error al guardar paciente: " + e.getMessage(), e);
                    Toast.makeText(AddEditPatientActivity.this, getString(R.string.error_saving_patient) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    private boolean isValidVisitDate(String dateString) {
        if (ValidationUtils.isEmpty(dateString)) {
            return false;
        }
        try {
            Date visitDate = dateFormatter.parse(dateString);
            Date currentDate = new Date();

            Calendar calVisit = Calendar.getInstance();
            calVisit.setTime(visitDate);
            calVisit.set(Calendar.HOUR_OF_DAY, 0);
            calVisit.set(Calendar.MINUTE, 0);
            calVisit.set(Calendar.SECOND, 0);
            calVisit.set(Calendar.MILLISECOND, 0);

            Calendar calCurrent = Calendar.getInstance();
            calCurrent.setTime(currentDate);
            calCurrent.set(Calendar.HOUR_OF_DAY, 0);
            calCurrent.set(Calendar.MINUTE, 0);
            calCurrent.set(Calendar.SECOND, 0);
            calCurrent.set(Calendar.MILLISECOND, 0);


            return !calVisit.before(calCurrent);

        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }


    private void showDeleteConfirmationDialog() {
        if (currentPatient == null || patientUid == null) {
            Toast.makeText(this, "No hay historial de paciente para eliminar.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete_patient_title))
                .setMessage(getString(R.string.confirm_delete_patient_message, currentPatient.getName()))
                .setPositiveButton(R.string.button_delete, (dialog, which) -> deletePatient())
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }


    private void deletePatient() {
        if (patientUid == null) {
            Toast.makeText(this, "Error: UID de paciente no disponible para eliminar.", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressBar();
        btnDeletePatient.setEnabled(false);
        btnSavePatient.setEnabled(false);

        String appId = getPackageName();
        db.collection("artifacts").document(appId)
                .collection("public").document("data")
                .collection("patients").document(patientUid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    hideProgressBar();
                    Toast.makeText(AddEditPatientActivity.this, R.string.success_patient_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    hideProgressBar();
                    btnDeletePatient.setEnabled(true);
                    btnSavePatient.setEnabled(true);
                    Log.e(TAG, "Error al eliminar paciente: " + e.getMessage(), e);
                    Toast.makeText(AddEditPatientActivity.this, getString(R.string.error_deleting_patient) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Agrega una cita al calendario del dispositivo.
     * Utiliza la fecha de visita, el nombre del paciente y el diagnóstico.
     */
    private void addAppointmentToCalendar() {
        String patientName = etPatientName.getText().toString().trim();
        String visitDateString = etPatientVisitDate.getText().toString().trim();
        String diagnosis = etPatientDiagnosis.getText().toString().trim();

        if (ValidationUtils.isEmpty(visitDateString)) {
            Toast.makeText(this, R.string.error_no_appointment_date, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Date visitDate = dateFormatter.parse(visitDateString);
            Calendar beginTime = Calendar.getInstance();
            beginTime.setTime(visitDate);
            beginTime.set(Calendar.HOUR_OF_DAY, 9);
            beginTime.set(Calendar.MINUTE, 0);

            Calendar endTime = Calendar.getInstance();
            endTime.setTime(visitDate);
            endTime.set(Calendar.HOUR_OF_DAY, 10);
            endTime.set(Calendar.MINUTE, 0);

            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                    .putExtra(CalendarContract.Events.TITLE, getString(R.string.calendar_event_title, patientName))
                    .putExtra(CalendarContract.Events.DESCRIPTION, getString(R.string.calendar_event_description, diagnosis))
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, etHospitalAddress.getText().toString().trim())
                    .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                Toast.makeText(this, R.string.success_calendar_add, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.error_calendar_add_failed, Toast.LENGTH_SHORT).show();
            }

        } catch (ParseException e) {
            Toast.makeText(this, R.string.error_invalid_visit_date, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
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
