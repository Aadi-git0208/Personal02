import { useEffect, useState } from "react";
import { getApiErrorMessage, ApiError } from "../../api/client";
import { getPatientProfile, savePatientProfile } from "../../api/profileApi";
import { useAuth } from "../../auth/AuthContext";
import "./BookAppointment.css";

const emptyForm = {
    fullName: "",
    phoneNumber: "",
    dateOfBirth: "",
    gender: "UNSPECIFIED",
    address: "",
    bloodGroup: ""
};

function PatientProfile() {
    const { user } = useAuth();
    const [form, setForm] = useState({
        ...emptyForm,
        fullName: user?.name || ""
    });
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    useEffect(() => {
        let cancelled = false;

        async function load() {
            setLoading(true);
            setError("");
            try {
                const profile = await getPatientProfile();
                if (cancelled) {
                    return;
                }
                setForm({
                    fullName: profile.fullName || user?.name || "",
                    phoneNumber: profile.phoneNumber || "",
                    dateOfBirth: profile.dateOfBirth || "",
                    gender: profile.gender || "UNSPECIFIED",
                    address: profile.address || "",
                    bloodGroup: profile.bloodGroup || ""
                });
            } catch (loadError) {
                if (cancelled) {
                    return;
                }
                if (loadError instanceof ApiError && loadError.status === 404) {
                    return;
                }
                setError(getApiErrorMessage(loadError));
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
    }, [user?.name]);

    function updateField(field, value) {
        setForm((current) => ({ ...current, [field]: value }));
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setError("");
        setSuccess("");
        setSaving(true);

        try {
            await savePatientProfile({
                fullName: form.fullName.trim(),
                phoneNumber: form.phoneNumber.trim(),
                dateOfBirth: form.dateOfBirth,
                gender: form.gender || "UNSPECIFIED",
                address: form.address.trim(),
                bloodGroup: form.bloodGroup
            });
            setSuccess("Profile saved.");
        } catch (saveError) {
            setError(getApiErrorMessage(saveError));
        } finally {
            setSaving(false);
        }
    }

    if (loading) {
        return <p>Loading your profile...</p>;
    }

    return (
        <div className="book-appointment-page">
            <h2 className="page-title">
                <span>My Profile</span>
                <small>Keep your contact and medical details up to date.</small>
            </h2>

            {error && <p role="alert">{error}</p>}
            {success && <p>{success}</p>}

            <div className="booking-wrapper">
                <form className="booking-form" onSubmit={handleSubmit}>
                    <input
                        type="text"
                        placeholder="Full name"
                        value={form.fullName}
                        onChange={(event) => updateField("fullName", event.target.value)}
                        required
                    />
                    <input
                        type="tel"
                        placeholder="Phone number"
                        value={form.phoneNumber}
                        onChange={(event) => updateField("phoneNumber", event.target.value)}
                        required
                    />
                    <input
                        type="date"
                        value={form.dateOfBirth}
                        onChange={(event) => updateField("dateOfBirth", event.target.value)}
                        required
                    />
                    <select
                        value={form.gender}
                        onChange={(event) => updateField("gender", event.target.value)}
                    >
                        <option value="UNSPECIFIED">Prefer not to say</option>
                        <option value="MALE">Male</option>
                        <option value="FEMALE">Female</option>
                        <option value="OTHER">Other</option>
                    </select>
                    <input
                        type="text"
                        placeholder="Address"
                        value={form.address}
                        onChange={(event) => updateField("address", event.target.value)}
                    />
                    <select
                        value={form.bloodGroup}
                        onChange={(event) => updateField("bloodGroup", event.target.value)}
                    >
                        <option value="">Blood group (optional)</option>
                        {["A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"].map((group) => (
                            <option key={group} value={group}>{group}</option>
                        ))}
                    </select>
                    <button type="submit" disabled={saving}>
                        {saving ? "Saving..." : "Save profile"}
                    </button>
                </form>
            </div>
        </div>
    );
}

export default PatientProfile;
