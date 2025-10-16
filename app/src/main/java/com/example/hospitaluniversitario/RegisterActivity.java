package com.example.hospitaluniversitario;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.hospitaluniversitario.models.User;
import com.example.hospitaluniversitario.utils.ValidationUtils;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Spinner spinnerUserRole;
    private Button btnRegister;
    private TextView tvBackToLoginLink;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String selectedRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        spinnerUserRole = findViewById(R.id.spinnerUserRole);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLoginLink = findViewById(R.id.tvBackToLoginLink);
        progressBar = findViewById(R.id.progressBar);

        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.user_roles,
                android.R.layout.simple_spinner_item
        );
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserRole.setAdapter(roleAdapter);

        int patientPosition = roleAdapter.getPosition(getString(R.string.patient_role));
        spinnerUserRole.setSelection(patientPosition);
        selectedRole = getString(R.string.patient_role);

        spinnerUserRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRole = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        btnRegister.setOnClickListener(v -> registerUser());
        tvBackToLoginLink.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (ValidationUtils.isEmpty(fullName)) {
            etFullName.setError(getString(R.string.error_empty_field));
            etFullName.requestFocus();
            return;
        }
        if (ValidationUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_empty_field));
            etEmail.requestFocus();
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return;
        }
        if (ValidationUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_empty_field));
            etPassword.requestFocus();
            return;
        }
        if (!ValidationUtils.isValidPassword(password, 6)) {
            etPassword.setError(getString(R.string.error_password_weak));
            etPassword.requestFocus();
            return;
        }
        if (!ValidationUtils.doPasswordsMatch(password, confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.error_password_match));
            etConfirmPassword.requestFocus();
            return;
        }

        showProgressBar();
        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    hideProgressBar();
                    btnRegister.setEnabled(true);

                    if (task.isSuccessful()) {
                        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                        saveUserProfile(uid, email, fullName, selectedRole);

                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(RegisterActivity.this, "El correo electrónico ya está registrado.", Toast.LENGTH_LONG).show();
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Error desconocido.";
                            Toast.makeText(RegisterActivity.this, getString(R.string.error_registration_failed, errorMessage), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveUserProfile(String uid, String email, String fullName, String role) {
        User newUser = new User(uid, email, fullName, role);

        String appId = getPackageName();

        db.collection("artifacts").document(appId)
                .collection("users").document(uid)
                .collection("user_profiles").document(uid)
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, R.string.success_registration, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Registro exitoso en Auth, pero falló guardar datos adicionales: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
