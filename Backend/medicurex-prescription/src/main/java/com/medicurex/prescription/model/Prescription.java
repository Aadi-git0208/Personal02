package com.medicurex.prescription.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "prescriptions",
        indexes = {
                @Index(name = "idx_rx_patient", columnList = "patient_user_id"),
                @Index(name = "idx_rx_doctor", columnList = "doctor_user_id"),
                @Index(name = "idx_rx_appointment", columnList = "appointment_id"),
                @Index(name = "idx_rx_status", columnList = "status"),
                @Index(name = "idx_rx_created", columnList = "created_at")
        }
)
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_user_id", nullable = false)
    private Long patientUserId;

    @Column(name = "doctor_user_id", nullable = false)
    private Long doctorUserId;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @Column(nullable = false, length = 1000)
    private String diagnosis;

    @Column(length = 2000)
    private String instructions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrescriptionStatus status = PrescriptionStatus.ACTIVE;

    @Column(name = "expires_on")
    private LocalDate expiresOn;

    @Column(name = "cancellation_reason", length = 255)
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<PrescriptionItem> items = new ArrayList<>();

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addItem(PrescriptionItem item) {
        item.setPrescription(this);
        items.add(item);
    }

    public boolean isActive() {
        return status == PrescriptionStatus.ACTIVE;
    }

    public Long getId() {
        return id;
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

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public PrescriptionStatus getStatus() {
        return status;
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }

    public LocalDate getExpiresOn() {
        return expiresOn;
    }

    public void setExpiresOn(LocalDate expiresOn) {
        this.expiresOn = expiresOn;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<PrescriptionItem> getItems() {
        return items;
    }
}
