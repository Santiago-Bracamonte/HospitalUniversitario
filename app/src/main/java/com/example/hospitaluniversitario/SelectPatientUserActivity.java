package com.example.hospitaluniversitario;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hospitaluniversitario.adapters.UserAdapter;
import com.example.hospitaluniversitario.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SelectPatientUserActivity extends AppCompatActivity implements UserAdapter.OnUserClickListener {

    private static final String TAG = "SelectPatientUserAct";

    private RecyclerView recyclerViewPatientUsers;
    private UserAdapter userAdapter;
    private List<User> allPatientUsers;
    private List<User> filteredPatientUsers;
    private EditText etSearchPatientUser;
    private ProgressBar progressBar;
    private TextView tvNoPatientUsersFound;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration patientUsersListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_patient_user);

        Log.d(TAG, "onCreate: Iniciando SelectPatientUserActivity");

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerViewPatientUsers = findViewById(R.id.recyclerViewPatientUsers);
        etSearchPatientUser = findViewById(R.id.etSearchPatientUser);
        progressBar = findViewById(R.id.progressBarSelectPatient);
        tvNoPatientUsersFound = findViewById(R.id.tvNoPatientUsersFound);

        recyclerViewPatientUsers.setLayoutManager(new LinearLayoutManager(this));
        allPatientUsers = new ArrayList<>();
        filteredPatientUsers = new ArrayList<>();
        userAdapter = new UserAdapter(filteredPatientUsers, this);
        recyclerViewPatientUsers.setAdapter(userAdapter);

        etSearchPatientUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPatientUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadPatientUsers();
    }

    private void loadPatientUsers() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_user_not_logged_in, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showProgressBar();

        if (patientUsersListener != null) {
            patientUsersListener.remove();
        }

        Query query = db.collectionGroup("user_profiles")
                .whereEqualTo("role", getString(R.string.patient_role));

        patientUsersListener = query.addSnapshotListener((snapshots, e) -> {
            hideProgressBar();
            if (e != null) {
                updateEmptyState();
                return;
            }

            allPatientUsers.clear();
            if (snapshots != null && !snapshots.isEmpty()) {
                for (QueryDocumentSnapshot doc : snapshots) {
                    try {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setUid(doc.getId());
                            allPatientUsers.add(user);
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "loadPatientUsers: Error al convertir documento a objeto User: " + doc.getId(), ex);
                    }
                }
            } else {
                Log.d(TAG, "loadPatientUsers: No se encontraron perfiles de paciente.");
            }

            filterPatientUsers(etSearchPatientUser.getText().toString());
        });
    }

    private void filterPatientUsers(String query) {
        Log.d(TAG, "filterPatientUsers: Filtrando con query: " + query);
        filteredPatientUsers.clear();
        if (query.isEmpty()) {
            filteredPatientUsers.addAll(allPatientUsers);
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
            for (User user : allPatientUsers) {
                boolean nameMatches = user.getName() != null && user.getName().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);
                boolean emailMatches = user.getEmail() != null && user.getEmail().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);

                if (nameMatches || emailMatches) {
                    filteredPatientUsers.add(user);
                }
            }
        }
        userAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredPatientUsers.isEmpty()) {
            tvNoPatientUsersFound.setVisibility(View.VISIBLE);
            recyclerViewPatientUsers.setVisibility(View.GONE);
            Log.d(TAG, "updateEmptyState: No se encontraron pacientes, mostrando mensaje de vacío.");
        } else {
            tvNoPatientUsersFound.setVisibility(View.GONE);
            recyclerViewPatientUsers.setVisibility(View.VISIBLE);
            Log.d(TAG, "updateEmptyState: Pacientes encontrados, mostrando RecyclerView.");
        }
    }

    @Override
    public void onUserClick(User user) {
        Log.d(TAG, "onUserClick: Paciente seleccionado: " + user.getName() + " (UID: " + user.getUid() + ")");
        Toast.makeText(this, getString(R.string.patient_selected_for_record, user.getName() != null ? user.getName() : user.getEmail()), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SelectPatientUserActivity.this, AddEditPatientActivity.class);
        intent.putExtra("patient_uid", user.getUid());
        startActivity(intent);
        finish();
    }

    private void showProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            Log.d(TAG, "showProgressBar: Mostrando ProgressBar.");
        }
    }

    private void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
            Log.d(TAG, "hideProgressBar: Ocultando ProgressBar.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (patientUsersListener != null) {
            patientUsersListener.remove();
            Log.d(TAG, "onDestroy: Listener de Firestore removido.");
        }
        Log.d(TAG, "onDestroy: SelectPatientUserActivity destruida.");
    }
}
