import { useEffect, useState } from "react";
import { getApiErrorMessage, pageContent } from "../../api/client";
import { listDoctorAppointments } from "../../api/appointmentApi";
import {
    cancelPrescription,
    createPrescription,
    listDoctorPrescriptions
} from "../../api/prescriptionApi";

const emptyItem = () => ({
    medicineName: "",
    dosage: "",
    frequency: "",
    duration: "",
    instructions: ""
});

export default function DoctorPrescription() {
    const [appointments, setAppointments] = useState([]);
    const [prescriptions, setPrescriptions] = useState([]);
    const [appointmentId, setAppointmentId] = useState("");
    const [diagnosis, setDiagnosis] = useState("");
    const [instructions, setInstructions] = useState("");
    const [medicines, setMedicines] = useState([emptyItem()]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    async function reload() {
        const [appointmentPage, prescriptionPage] = await Promise.all([
            listDoctorAppointments({ size: 50, sort: "appointmentDate,desc" }),
            listDoctorPrescriptions({ size: 50, sort: "createdAt,desc" })
        ]);
        setAppointments(
            pageContent(appointmentPage).filter(
                (appointment) => appointment.status === "CONFIRMED" || appointment.status === "COMPLETED"
            )
        );
        setPrescriptions(pageContent(prescriptionPage));
    }

    useEffect(() => {
        let cancelled = false;

        async function load() {
            setLoading(true);
            try {
                await reload();
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

    const addRow = () => {
        setMedicines([...medicines, emptyItem()]);
    };

    const handleChange = (index, field, value) => {
        const copy = [...medicines];
        copy[index][field] = value;
        setMedicines(copy);
    };

    const savePrescription = async () => {
        setError("");
        setSuccess("");

        const items = medicines
            .filter((item) => item.medicineName.trim() && item.dosage.trim() && item.frequency.trim() && item.duration.trim())
            .map((item) => ({
                medicineName: item.medicineName.trim(),
                dosage: item.dosage.trim(),
                frequency: item.frequency.trim(),
                duration: item.duration.trim(),
                instructions: item.instructions.trim() || undefined
            }));

        if (!appointmentId || !diagnosis.trim() || items.length === 0) {
            setError("Choose an eligible appointment, enter a diagnosis, and add at least one complete medicine.");
            return;
        }

        setSaving(true);
        try {
            await createPrescription({
                appointmentId: Number(appointmentId),
                diagnosis: diagnosis.trim(),
                instructions: instructions.trim() || undefined,
                items
            });
            setSuccess("Prescription saved.");
            setDiagnosis("");
            setInstructions("");
            setMedicines([emptyItem()]);
            await reload();
        } catch (saveError) {
            setError(getApiErrorMessage(saveError));
        } finally {
            setSaving(false);
        }
    };

    const handleCancel = async (prescriptionId) => {
        setError("");
        try {
            await cancelPrescription(prescriptionId, "Cancelled by doctor");
            await reload();
        } catch (cancelError) {
            setError(getApiErrorMessage(cancelError));
        }
    };

    return (
        <div>
            <style>{css}</style>
            <div className="page">
                <h2> Write Prescription</h2>
                {error && <p role="alert">{error}</p>}
                {success && <p>{success}</p>}
                {loading ? (
                    <p>Loading eligible appointments...</p>
                ) : (
                    <div className="card">
                        <select
                            value={appointmentId}
                            onChange={(event) => setAppointmentId(event.target.value)}
                        >
                            <option value="">Select an eligible appointment</option>
                            {appointments.map((appointment) => (
                                <option key={appointment.id} value={appointment.id}>
                                    #{appointment.id} · Patient #{appointment.patientUserId} · {appointment.appointmentDate} · {appointment.status}
                                </option>
                            ))}
                        </select>
                        {appointments.length === 0 && (
                            <p>No confirmed or completed appointments are available for a prescription.</p>
                        )}
                        <textarea
                            placeholder="Diagnosis"
                            value={diagnosis}
                            onChange={(event) => setDiagnosis(event.target.value)}
                        />
                        <textarea
                            placeholder="Instructions (optional)"
                            value={instructions}
                            onChange={(event) => setInstructions(event.target.value)}
                        />
                        <table>
                            <thead>
                                <tr>
                                    <th>Medicine</th>
                                    <th>Dosage</th>
                                    <th>Frequency</th>
                                    <th>Duration</th>
                                    <th>Notes</th>
                                </tr>
                            </thead>
                            <tbody>
                                {medicines.map((item, index) => (
                                    <tr key={index}>
                                        <td><input value={item.medicineName} onChange={(event) => handleChange(index, "medicineName", event.target.value)} /></td>
                                        <td><input value={item.dosage} onChange={(event) => handleChange(index, "dosage", event.target.value)} /></td>
                                        <td><input value={item.frequency} onChange={(event) => handleChange(index, "frequency", event.target.value)} /></td>
                                        <td><input value={item.duration} onChange={(event) => handleChange(index, "duration", event.target.value)} /></td>
                                        <td><input value={item.instructions} onChange={(event) => handleChange(index, "instructions", event.target.value)} /></td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                        <button onClick={addRow} className="btn2" type="button">+ Add Medicine</button>
                        <button onClick={savePrescription} className="btn" type="button" disabled={saving}>
                            {saving ? "Saving..." : "Save Prescription"}
                        </button>
                    </div>
                )}

                <div className="card" style={{ marginTop: "24px" }}>
                    <h3>Recent prescriptions</h3>
                    {prescriptions.length === 0 ? (
                        <p>No prescriptions yet.</p>
                    ) : (
                        prescriptions.map((prescription) => (
                            <div key={prescription.id}>
                                <p>
                                    #{prescription.id} · Patient #{prescription.patientUserId} · {prescription.status} · {prescription.diagnosis}
                                </p>
                                {prescription.status === "ACTIVE" && (
                                    <button type="button" className="btn2" onClick={() => handleCancel(prescription.id)}>
                                        Cancel
                                    </button>
                                )}
                            </div>
                        ))
                    )}
                </div>
            </div>
        </div>
    );
}

const css = `
.page {
  padding: 30px;
  color: white;
  background: #eae7dc;
  min-height: 100vh;
  font-family: Arial;
  background: border-box;
}

.card {
  background: #f4efe2;
  padding: 25px;
  border-radius: 20px;
  max-width: 900px;
  box-shadow: 0 0 20px rgba(0,0,0,0.5);
}

input, textarea, select {
  width: 100%;
  padding: 10px;
  margin-bottom: 12px;
  border-radius: 8px;
  border: none;
}

textarea {
  height: 80px;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin: 15px 0;
}

th, td {
  border-bottom: 1px solid #334;
  padding: 8px;
  text-align: left;
}

.btn {
  background: linear-gradient(90deg, #00c6ff, #0072ff);
  border: none;
  padding: 12px 20px;
  color: white;
  border-radius: 10px;
  cursor: pointer;
  font-weight: bold;
}

.btn2 {
  background: #444;
  margin-right: 10px;
  border: none;
  padding: 12px 15px;
  color: white;
  border-radius: 10px;
  cursor: pointer;
}
`;
