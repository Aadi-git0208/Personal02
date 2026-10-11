import React, { useEffect, useState } from "react";
import { getApiErrorMessage } from "../../api/client";
import { getAdminDirectory, updateDoctorApproval } from "../../api/profileApi";
import "./Admin.css";

const ManageUsers = () => {
    const [patients, setPatients] = useState([]);
    const [doctors, setDoctors] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [pendingId, setPendingId] = useState(null);

    async function loadDirectory() {
        setLoading(true);
        setError("");
        try {
            const directory = await getAdminDirectory();
            setPatients(directory?.patients || []);
            setDoctors(directory?.doctors || []);
        } catch (loadError) {
            setError(getApiErrorMessage(loadError));
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadDirectory();
    }, []);

    async function setApproval(authUserId, status) {
        setPendingId(authUserId);
        setError("");
        try {
            await updateDoctorApproval(authUserId, status);
            await loadDirectory();
        } catch (approvalError) {
            setError(getApiErrorMessage(approvalError));
        } finally {
            setPendingId(null);
        }
    }

    return (
        <div className="admin-page">
            <h1>Manage Users</h1>
            {error && <p role="alert">{error}</p>}
            <div className="admin-section">
                <h2>Doctor approval</h2>
                <p>
                    Auth does not expose a user-administration API. This screen uses the Profile Service directory and approval endpoint only.
                </p>
                {loading && <p>Loading profiles...</p>}
                {!loading && doctors.length === 0 && <p className="admin-empty">No doctor profiles yet</p>}
                {doctors.map((doctor) => (
                    <div className="admin-row" key={doctor.id}>
                        <span>
                            {doctor.fullName} · {doctor.specialization} · {doctor.approvalStatus}
                        </span>
                        <span>
                            {doctor.approvalStatus !== "APPROVED" && (
                                <button
                                    className="admin-btn"
                                    disabled={pendingId === doctor.authUserId}
                                    onClick={() => setApproval(doctor.authUserId, "APPROVED")}
                                >
                                    Approve
                                </button>
                            )}
                            {doctor.approvalStatus !== "REJECTED" && (
                                <button
                                    className="admin-btn admin-btn-danger"
                                    disabled={pendingId === doctor.authUserId}
                                    onClick={() => setApproval(doctor.authUserId, "REJECTED")}
                                >
                                    Reject
                                </button>
                            )}
                        </span>
                    </div>
                ))}
            </div>
            <div className="admin-section">
                <h2>Patient profiles</h2>
                {patients.length === 0 ? (
                    <p className="admin-empty">No patient profiles yet</p>
                ) : (
                    patients.map((patient) => (
                        <div className="admin-row" key={patient.id}>
                            <span>{patient.fullName} · {patient.phoneNumber}</span>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
};

export default ManageUsers;
