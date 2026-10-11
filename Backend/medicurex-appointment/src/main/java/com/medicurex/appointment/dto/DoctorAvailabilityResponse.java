package com.medicurex.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class DoctorAvailabilityResponse {

    private Long doctorProfileId;
    private Long doctorUserId;
    private LocalDate date;
    private List<String> advertisedSlots;
    private List<BookedRange> bookedRanges;

    public DoctorAvailabilityResponse(
            Long doctorProfileId,
            Long doctorUserId,
            LocalDate date,
            List<String> advertisedSlots,
            List<BookedRange> bookedRanges
    ) {
        this.doctorProfileId = doctorProfileId;
        this.doctorUserId = doctorUserId;
        this.date = date;
        this.advertisedSlots = advertisedSlots;
        this.bookedRanges = bookedRanges;
    }

    public Long getDoctorProfileId() {
        return doctorProfileId;
    }

    public Long getDoctorUserId() {
        return doctorUserId;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<String> getAdvertisedSlots() {
        return advertisedSlots;
    }

    public List<BookedRange> getBookedRanges() {
        return bookedRanges;
    }

    public static class BookedRange {
        private LocalTime startTime;
        private LocalTime endTime;
        private String status;

        public BookedRange(LocalTime startTime, LocalTime endTime, String status) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.status = status;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        public String getStatus() {
            return status;
        }
    }
}
