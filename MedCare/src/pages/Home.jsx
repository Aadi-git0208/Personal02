import React from 'react';
import './Home.css';

const Home = () => {
  return (
    <section className="main-home">
      <div className="bg-video">
        <video autoPlay muted loop playsInline className="video" preload="metadata">
          <source src="/Bg.mp4" type="video/mp4" />
        </video>

        <div className="overlay">
          <h1>Welcome to MediCurex</h1>
          <p>
            MediCurex is a digital healthcare platform that connects patients and doctors on a single secure interface. It allows users to book appointments, consult doctors online, manage medical records, and access medicines easily, ensuring fast, reliable, and technology-driven healthcare services.
.
          </p>
        </div>
      </div>
    </section>
  );
};

export default Home;
