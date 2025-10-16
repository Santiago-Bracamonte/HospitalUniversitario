package com.example.hospitaluniversitario;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.hospitaluniversitario.models.User;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private TextView tvWelcomeUser, tvUserId;
    private Button btnManagePatients, btnLogout, btnViewHospitalLocation;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvUserId = findViewById(R.id.tvUserId);
        btnManagePatients = findViewById(R.id.btnManagePatients);
        btnLogout = findViewById(R.id.btnLogout);
        btnViewHospitalLocation = findViewById(R.id.btnViewHospitalLocation);

        btnManagePatients.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PatientListActivity.class)));
        btnLogout.setOnClickListener(v -> logoutUser());
        btnViewHospitalLocation.setOnClickListener(v -> viewHospitalLocation());

        loadUserProfile();
    }

    private void loadUserProfile() {
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }


        String appId = getPackageName();

        DocumentReference userDocRef = db.collection("artifacts").document(appId)
                .collection("users").document(currentUser.getUid())
                .collection("user_profiles").document(currentUser.getUid());

        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User userProfile = documentSnapshot.toObject(User.class);
                if (userProfile != null) {
                    String userName = userProfile.getName() != null && !userProfile.getName().isEmpty() ? userProfile.getName() : currentUser.getEmail();

                    tvWelcomeUser.setText(getString(R.string.welcome_message, userName, getString(R.string.doctor_role)));
                } else {
                    tvWelcomeUser.setText(getString(R.string.welcome_message, currentUser.getEmail(), "error_profile_null"));
                }
            } else {
                tvWelcomeUser.setText(getString(R.string.welcome_message, currentUser.getEmail(), getString(R.string.doctor_role)));
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al cargar perfil de usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            tvWelcomeUser.setText(getString(R.string.welcome_message, currentUser.getEmail(), "error_loading"));
        });
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Sesión cerrada.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    private void viewHospitalLocation() {
        String mapUrl = "https://maps.app.goo.gl/VNxfqX7MUeMZ2zRs5";

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl));
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, R.string.no_map_app_found, Toast.LENGTH_SHORT).show();
        }
    }
}
