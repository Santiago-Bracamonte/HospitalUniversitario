package com.example.hospitaluniversitario;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.hospitaluniversitario.models.User;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> loginUser());
        tvRegisterLink.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            redirectUserBasedOnRole(currentUser.getUid());
        }
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_field, Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressBar();
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    hideProgressBar();
                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(LoginActivity.this, R.string.success_login, Toast.LENGTH_SHORT).show();
                            redirectUserBasedOnRole(user.getUid());
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Error desconocido.";
                        Toast.makeText(LoginActivity.this, getString(R.string.error_login_failed, errorMessage), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void redirectUserBasedOnRole(String uid) {
        String appId = getPackageName();
        DocumentReference userDocRef = db.collection("artifacts").document(appId)
                .collection("users").document(uid)
                .collection("user_profiles").document(uid);

        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User userProfile = documentSnapshot.toObject(User.class);
                if (userProfile != null && userProfile.getRole() != null) {
                    String role = userProfile.getRole();
                    Intent intent;
                    if (role.equalsIgnoreCase(getString(R.string.doctor_role))) {
                        intent = new Intent(LoginActivity.this, MainActivity.class);
                    } else if (role.equalsIgnoreCase(getString(R.string.patient_role))) {
                        intent = new Intent(LoginActivity.this, PatientHomeActivity.class);
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.error_unrecognized_role, Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        return;
                    }
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, R.string.error_user_profile_not_found, Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                }
            } else {
                Toast.makeText(LoginActivity.this, R.string.error_user_profile_not_found, Toast.LENGTH_LONG).show();
                mAuth.signOut();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(LoginActivity.this, "Error al cargar perfil para redirección: " + e.getMessage(), Toast.LENGTH_LONG).show();
            mAuth.signOut();
            startActivity(new Intent(LoginActivity.this, LoginActivity.class));
            finish();
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
