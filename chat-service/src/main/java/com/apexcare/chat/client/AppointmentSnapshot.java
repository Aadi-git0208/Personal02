package com.apexcare.chat.client;

public class AppointmentSnapshot {

    private Long id;
    private Long patientId;
    private Long doctorId;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean allowsChat() {
        if (status == null) {
            return false;
        }
        return switch (status.toUpperCase()) {
            case "PENDING", "CONFIRMED", "COMPLETED" -> true;
            default -> false;
        };
    }
}
