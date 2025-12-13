import React from 'react';
import './Home.css';
import Services from '../components/Services';  

const Home = () => {
  return (
    <>
    <section className="main-home">
      <div className="bg-video">
        <video autoPlay muted loop playsInline className="video" preload="metadata">
          <source src="/Bg.mp4" type="video/mp4" />
        </video>

        <div className="overlay">
          <h1>Welcome to MediCurex</h1>
          <p>
            MediCurex is a digital healthcare platform that connects patients and doctors on a single secure interface. It allows users to book appointments, consult doctors online, manage medical records, and access medicines easily, ensuring fast, reliable, and technology-driven healthcare services.
          </p>
        </div>
      </div>
    </section>
    
 
    <section className="About-Section">
      <div className="About-Content">
        <h2>ABOUT US</h2>
        <div className="underline"></div>
        <p>
          At MediCurex, we are dedicated to revolutionizing healthcare by leveraging technology to provide seamless access to medical services. Our platform is designed to bridge the gap between patients and healthcare providers, ensuring that quality care is just a click away. Whether you need to consult with a specialist, manage your health records, or order medications, MediCurex is here to make healthcare more accessible and efficient for everyone.
        </p>
      </div>
    </section>
    
        <h2>MY SERVICES</h2>
          <Services />
    
    <section className="footer">
      <p>&copy; 2025 MediCurex. All rights reserved.</p>
    </section>
   
    </>
  );
};

export default Home;
