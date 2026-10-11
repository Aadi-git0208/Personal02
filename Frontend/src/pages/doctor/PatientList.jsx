import { useEffect, useMemo, useState } from "react";
import { getApiErrorMessage, pageContent } from "../../api/client";
import { listDoctorAppointments } from "../../api/appointmentApi";

const PatientList = () => {
    const [appointments, setAppointments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let cancelled = false;

        async function load() {
            try {
                const page = await listDoctorAppointments({ size: 50, sort: "appointmentDate,desc" });
                if (!cancelled) {
                    setAppointments(pageContent(page));
                }
            } catch (loadError) {
                if (!cancelled) {
                    setError(getApiErrorMessage(loadError));
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        }

        load();
        return () => {
            cancelled = true;
        };
    }, []);

    const patients = useMemo(() => {
        const map = new Map();
        appointments.forEach((appointment) => {
            if (!map.has(appointment.patientUserId)) {
                map.set(appointment.patientUserId, {
                    id: appointment.patientUserId,
                    appointmentCount: 1,
                    latestDate: appointment.appointmentDate
                });
            } else {
                const current = map.get(appointment.patientUserId);
                current.appointmentCount += 1;
            }
        });
        return Array.from(map.values());
    }, [appointments]);

    return (
        <div className="doctor-dashboard no-sidebar">
            <main className="main-content">
                <h1>My Patients</h1>
                <p className="subtitle">Patients who have booked appointments with you</p>
                {loading && <p>Loading patients...</p>}
                {error && <p role="alert">{error}</p>}
                {!loading && patients.length === 0 && <p>No patients yet.</p>}
                {patients.map((patient) => (
                    <p key={patient.id}>
                        Patient #{patient.id} · {patient.appointmentCount} appointment(s)
                    </p>
                ))}
                <p>
                    Patient names are not returned by the Appointment API. This list is derived from appointment ownership only.
                </p>
            </main>
        </div>
    );
};

export default PatientList;
