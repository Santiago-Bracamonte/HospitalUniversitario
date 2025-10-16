package com.example.hospitaluniversitario.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hospitaluniversitario.R;
import com.example.hospitaluniversitario.models.Patient;

import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private List<Patient> patientList;
    private OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(Patient patient);
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public PatientAdapter(List<Patient> patientList) {
        this.patientList = patientList;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_list_item, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patientList.get(position);
        holder.bind(patient);
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    public void updatePatients(List<Patient> newPatients) {
        this.patientList.clear();
        this.patientList.addAll(newPatients);
        notifyDataSetChanged();
    }


    class PatientViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPatientName;
        private TextView tvPatientDetails;
        private TextView tvPatientDiagnosis;
        private TextView tvPatientVisitDate;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvPatientDetails = itemView.findViewById(R.id.tvPatientDetails);
            tvPatientDiagnosis = itemView.findViewById(R.id.tvPatientDiagnosis);
            tvPatientVisitDate = itemView.findViewById(R.id.tvPatientVisitDate);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(patientList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Patient patient) {
            tvPatientName.setText(patient.getName());
            String details = "Edad: " + patient.getAge() + " | Habitación: " + patient.getRoomNumber();
            tvPatientDetails.setText(details);
            tvPatientDiagnosis.setText("Diagnóstico: " + patient.getDiagnosis());
            tvPatientVisitDate.setText("Última Visita: " + patient.getVisitDate());
        }
    }
}
