import { useEffect, useState } from "react";
import { getApiErrorMessage, pageContent } from "../../api/client";
import { getPatientPrescription, listPatientPrescriptions } from "../../api/prescriptionApi";

function Prescription() {
    const [prescriptions, setPrescriptions] = useState([]);
    const [selected, setSelected] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let cancelled = false;

        async function load() {
            setLoading(true);
            try {
                const page = await listPatientPrescriptions({ size: 50, sort: "createdAt,desc" });
                if (!cancelled) {
                    setPrescriptions(pageContent(page));
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

    async function openDetail(prescriptionId) {
        setError("");
        try {
            const detail = await getPatientPrescription(prescriptionId);
            setSelected(detail);
        } catch (detailError) {
            setError(getApiErrorMessage(detailError));
        }
    }

    if (loading) {
        return <p>Loading prescriptions...</p>;
    }

    if (error) {
        return <p role="alert">{error}</p>;
    }

    if (prescriptions.length === 0) {
        return (
            <div className="no-appointments">
                <h3>No prescriptions yet</h3>
                <p>Prescriptions written by your doctor will appear here.</p>
            </div>
        );
    }

    return (
        <div className="my-appointments">
            <h2 className="appointments-title">My Prescriptions</h2>
            {selected ? (
                <div className="appointment-card">
                    <div className="appointment-details">
                        <p><strong>Status:</strong> {selected.status}</p>
                        <p><strong>Diagnosis:</strong> {selected.diagnosis}</p>
                        <p><strong>Instructions:</strong> {selected.instructions || "None"}</p>
                        <p><strong>Expires:</strong> {selected.expiresOn || "Not specified"}</p>
                        <ul>
                            {(selected.items || []).map((item) => (
                                <li key={item.id}>
                                    {item.medicineName} — {item.dosage}, {item.frequency}, {item.duration}
                                    {item.instructions ? ` (${item.instructions})` : ""}
                                </li>
                            ))}
                        </ul>
                        <button type="button" onClick={() => setSelected(null)}>Back to list</button>
                    </div>
                </div>
            ) : (
                <div className="appointments-list">
                    {prescriptions.map((prescription) => (
                        <div className="appointment-card" key={prescription.id}>
                            <div className="appointment-details">
                                <p><strong>Diagnosis:</strong> {prescription.diagnosis}</p>
                                <p><strong>Status:</strong> {prescription.status}</p>
                                <p><strong>Created:</strong> {prescription.createdAt ? new Date(prescription.createdAt).toLocaleDateString() : "N/A"}</p>
                            </div>
                            <button type="button" onClick={() => openDetail(prescription.id)}>
                                View details
                            </button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

export default Prescription;
