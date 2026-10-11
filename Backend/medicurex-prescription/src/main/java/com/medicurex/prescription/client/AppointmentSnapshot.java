package com.medicurex.prescription.client;

public class AppointmentSnapshot {

    private Long id;
    private Long patientUserId;
    private Long doctorUserId;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientUserId() {
        return patientUserId;
    }

    public void setPatientUserId(Long patientUserId) {
        this.patientUserId = patientUserId;
    }

    public Long getDoctorUserId() {
        return doctorUserId;
    }

    public void setDoctorUserId(Long doctorUserId) {
        this.doctorUserId = doctorUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
