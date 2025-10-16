package com.example.hospitaluniversitario.models;


public class Patient {
    private String id;
    private String name;
    private String age;
    private String gender;
    private String diagnosis;
    private String roomNumber;
    private String visitDate;
    private String attendingDoctor;
    private String hospitalLocationAddress;



    public Patient() {
    }

    public Patient(String id, String name, String age, String gender, String diagnosis, String roomNumber,
                   String visitDate, String attendingDoctor, String hospitalLocationAddress) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.diagnosis = diagnosis;
        this.roomNumber = roomNumber;
        this.visitDate = visitDate;
        this.attendingDoctor = attendingDoctor;
        this.hospitalLocationAddress = hospitalLocationAddress;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public String getAttendingDoctor() {
        return attendingDoctor;
    }

    public String getHospitalLocationAddress() {
        return hospitalLocationAddress;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public void setAttendingDoctor(String attendingDoctor) {
        this.attendingDoctor = attendingDoctor;
    }

    public void setHospitalLocationAddress(String hospitalLocationAddress) {
        this.hospitalLocationAddress = hospitalLocationAddress;
    }
}
