package com.medicurex.pharmacy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "stock_adjustments",
        uniqueConstraints = @UniqueConstraint(name = "uk_stock_operation_key", columnNames = "operation_key"),
        indexes = @Index(name = "idx_stock_adj_medicine", columnList = "medicine_id")
)
public class StockAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_key", nullable = false, length = 180)
    private String operationKey;

    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;

    @Column(nullable = false)
    private int delta;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getOperationKey() {
        return operationKey;
    }

    public void setOperationKey(String operationKey) {
        this.operationKey = operationKey;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public int getDelta() {
        return delta;
    }

    public void setDelta(int delta) {
        this.delta = delta;
    }
}
