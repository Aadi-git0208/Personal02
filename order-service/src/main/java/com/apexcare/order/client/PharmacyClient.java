package com.apexcare.order.client;

public interface PharmacyClient {

    MedicineSnapshot getMedicine(Long medicineId, String authorizationHeader);

    MedicineSnapshot adjustStock(Long medicineId, int adjustment, String authorizationHeader);
}
