package com.apexcare.pharmacy.service;

import com.apexcare.pharmacy.dto.MedicineRequest;
import com.apexcare.pharmacy.dto.UpdateStockRequest;
import com.apexcare.pharmacy.entity.Medicine;
import com.apexcare.pharmacy.exception.ApiException;
import com.apexcare.pharmacy.repository.MedicineRepository;
import com.apexcare.pharmacy.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicineServiceTest {

    @Mock
    private MedicineRepository medicineRepository;

    private MedicineService medicineService;

    @BeforeEach
    void setUp() {
        medicineService = new MedicineService(medicineRepository);
    }

    @Test
    void createsMedicineFromRequest() {
        when(medicineRepository.existsByNameIgnoreCaseAndActiveTrue("Paracetamol")).thenReturn(false);
        when(medicineRepository.save(any(Medicine.class))).thenAnswer(invocation -> {
            Medicine medicine = invocation.getArgument(0);
            medicine.setId(3L);
            return medicine;
        });

        var response = medicineService.create(request("Paracetamol", "50.00", 20));

        assertThat(response.getId()).isEqualTo(3L);
        assertThat(response.getName()).isEqualTo("Paracetamol");
        assertThat(response.getPrice()).isEqualByComparingTo("50.00");
        assertThat(response.getStockQuantity()).isEqualTo(20);
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void rejectsDuplicateActiveName() {
        when(medicineRepository.existsByNameIgnoreCaseAndActiveTrue("Paracetamol")).thenReturn(true);

        assertThatThrownBy(() -> medicineService.create(request("Paracetamol", "10.00", 5)))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void decrementsStockForOrderAdjustment() {
        Medicine medicine = medicine(3L, 10);
        when(medicineRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(medicine));
        when(medicineRepository.save(medicine)).thenReturn(medicine);

        UpdateStockRequest request = new UpdateStockRequest();
        request.setAdjustment(-3);

        var response = medicineService.updateStock(3L, request, patient());

        assertThat(response.getStockQuantity()).isEqualTo(7);
        ArgumentCaptor<Medicine> captor = ArgumentCaptor.forClass(Medicine.class);
        verify(medicineRepository).save(captor.capture());
        assertThat(captor.getValue().getStockQuantity()).isEqualTo(7);
    }

    @Test
    void rejectsAdjustmentBelowZero() {
        when(medicineRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(medicine(3L, 2)));
        UpdateStockRequest request = new UpdateStockRequest();
        request.setAdjustment(-5);

        assertThatThrownBy(() -> medicineService.updateStock(3L, request, patient()))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void softDeleteHidesMedicineFromCatalog() {
        Medicine medicine = medicine(3L, 10);
        when(medicineRepository.findById(3L)).thenReturn(Optional.of(medicine));
        when(medicineRepository.save(medicine)).thenReturn(medicine);

        medicineService.softDelete(3L);

        assertThat(medicine.isActive()).isFalse();
    }

    private static AuthenticatedUser patient() {
        return new AuthenticatedUser(11L, "rahul@gmail.com", "patient");
    }

    private static MedicineRequest request(String name, String price, int stock) {
        MedicineRequest request = new MedicineRequest();
        request.setName(name);
        request.setPrice(new BigDecimal(price));
        request.setStockQuantity(stock);
        request.setCategory("Analgesic");
        return request;
    }

    private static Medicine medicine(Long id, int stock) {
        Medicine medicine = new Medicine();
        medicine.setId(id);
        medicine.setName("Paracetamol");
        medicine.setPrice(new BigDecimal("50.00"));
        medicine.setStockQuantity(stock);
        medicine.setActive(true);
        return medicine;
    }
}
