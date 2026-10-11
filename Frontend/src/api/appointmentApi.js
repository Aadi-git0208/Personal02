import { apiRequest } from "./client";

export function parseSlotTimes(slotLabel) {
    const match = String(slotLabel || "")
        .trim()
        .match(/^(\d{1,2}):(\d{2})\s*(AM|PM)$/i);

    if (!match) {
        return null;
    }

    let hours = Number(match[1]);
    const minutes = Number(match[2]);
    const period = match[3].toUpperCase();

    if (period === "PM" && hours !== 12) {
        hours += 12;
    }
    if (period === "AM" && hours === 12) {
        hours = 0;
    }

    const startMinutes = hours * 60 + minutes;
    const endMinutes = startMinutes + 30;
    const pad = (value) => String(value).padStart(2, "0");

    return {
        startTime: `${pad(Math.floor(startMinutes / 60))}:${pad(startMinutes % 60)}:00`,
        endTime: `${pad(Math.floor(endMinutes / 60))}:${pad(endMinutes % 60)}:00`,
        slotLabel
    };
}

export function createAppointment(payload) {
    return apiRequest("/api/appointments", {
        method: "POST",
        body: payload
    });
}

export function listPatientAppointments(query) {
    return apiRequest("/api/appointments/patient/me", {
        method: "GET",
        query
    });
}

export function listDoctorAppointments(query) {
    return apiRequest("/api/appointments/doctor/me", {
        method: "GET",
        query
    });
}

export function listAdminAppointments(query) {
    return apiRequest("/api/appointments/admin", {
        method: "GET",
        query
    });
}

export function getAppointment(appointmentId) {
    return apiRequest(`/api/appointments/${appointmentId}`, {
        method: "GET"
    });
}

export function getDoctorAvailability(doctorProfileId, date) {
    return apiRequest(`/api/appointments/doctor/${doctorProfileId}/availability`, {
        method: "GET",
        query: { date }
    });
}

export function cancelAppointment(appointmentId, reason) {
    return apiRequest(`/api/appointments/${appointmentId}/cancel`, {
        method: "PATCH",
        body: reason ? { reason } : {}
    });
}

export function confirmAppointment(appointmentId) {
    return apiRequest(`/api/appointments/${appointmentId}/confirm`, {
        method: "PATCH"
    });
}

export function rejectAppointment(appointmentId, reason) {
    return apiRequest(`/api/appointments/${appointmentId}/reject`, {
        method: "PATCH",
        body: reason ? { reason } : {}
    });
}

export function completeAppointment(appointmentId) {
    return apiRequest(`/api/appointments/${appointmentId}/complete`, {
        method: "PATCH"
    });
}
