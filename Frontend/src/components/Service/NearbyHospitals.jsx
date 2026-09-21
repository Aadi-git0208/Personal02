import React, { useEffect, useState } from "react";
import { MapContainer, TileLayer, Marker } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import "./leafletFix";
import L from "leaflet";
import hospitalIconImg from "../../assets/hospital.png";

// 🏥 Custom hospital icon
const hospitalIcon = new L.Icon({
  iconUrl: hospitalIconImg,
  iconSize: [36, 36],
  iconAnchor: [18, 36],
});

const NearbyHospitals = () => {
  const [location, setLocation] = useState(null);
  const [hospitals, setHospitals] = useState([]);
  const [selectedHospital, setSelectedHospital] = useState(null);

  // 📍 Get user location
  useEffect(() => {
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setLocation([pos.coords.latitude, pos.coords.longitude]);
      },
      () => alert("Please allow location access")
    );
  }, []);

  // 🏥 Fetch nearby hospitals
  useEffect(() => {
    if (!location) return;

    const query = `
      [out:json];
      node["amenity"="hospital"](around:5000,${location[0]},${location[1]});
      out;
    `;

    fetch("https://overpass-api.de/api/interpreter", {
      method: "POST",
      body: query,
    })
      .then((res) => res.json())
      .then((data) => setHospitals(data.elements || []));
  }, [location]);

  if (!location) return <h3>Loading map...</h3>;

  return (
    <>
      
      <MapContainer center={location} zoom={13} style={{ height: "90vh" }}>
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution="© OpenStreetMap contributors"
        />

        <Marker position={location} />

        {hospitals.map((h) => (
          <Marker
            key={h.id}
            position={[h.lat, h.lon]}
            icon={hospitalIcon}
            eventHandlers={{
              click: () => setSelectedHospital(h),
            }}
          />
        ))}
      </MapContainer>

      {selectedHospital && (
        <div className="hospital-details">
          <h3>{selectedHospital.tags?.name || "Hospital"}</h3>

          <p>
            📍 Address:{" "}
            {selectedHospital.tags?.["addr:street"] ||
              selectedHospital.tags?.["addr:full"] ||
              "Not available"}
          </p>

          <p>📞 Phone: {selectedHospital.tags?.phone || "Not available"}</p>

          <p>
            🏥 Type:{" "}
            {selectedHospital.tags?.healthcare || "General Hospital"}
          </p>

          <div className="hospital-actions">
            <button
              onClick={() =>
                window.open(
                  `https://www.google.com/maps/search/?api=1&query=${selectedHospital.lat},${selectedHospital.lon}`,
                  "_blank"
                )
              }
            >
              Get Directions
            </button>

            <button onClick={() => setSelectedHospital(null)}>
              Close
            </button>
          </div>
        </div>
      )}
    </>
  );
};

export default NearbyHospitals;
