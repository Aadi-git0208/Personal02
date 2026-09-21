import React from "react";

const PatientList = () => {
  const pageStyle = {
    minHeight: "80vh",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    padding: "24px",
    textAlign: "center",
  };

  const cardStyle = {
    width: "100%",
    maxWidth: "720px",
  };

  const titleStyle = {
    fontSize: "3rem",
    marginBottom: "12px",
    color: "#f3f6ff",
    letterSpacing: "1px",
  };

  const subtitleStyle = {
    fontSize: "1.1rem",
    color: "#d7e2ff",
    lineHeight: "1.7",
    margin: 0,
  };

  const sectionStyle = {
    fontSize: "2.4rem",
    marginBottom: "22px",
    color: "#c17bff",
  };

  return (
    <div style={pageStyle}>
      <div style={cardStyle}>
        <h2 style={sectionStyle}>Patients</h2>

        <h1 style={titleStyle}>COMING SOON</h1>
        <p style={subtitleStyle}>
          This feature is not available yet. It will be available soon.
        </p>
      </div>
    </div>
  );
};

export default PatientList;
