package com.apexcare.prescription.client;

public interface PharmacyClient {

    MedicineSnapshot getMedicine(Long medicineId, String authorizationHeader);
}
