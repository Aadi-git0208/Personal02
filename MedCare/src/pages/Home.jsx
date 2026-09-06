import React, { useState, useEffect } from "react";
import "./Home.css";
import Services from "../components/ServicesDashboard";

const Home = () => {
    const aboutImages = [
        "/About-Section-img.jpg",
        "/Rahul.jpg",
        "/Rakesh.jpg",
        "/Nandani.jpg",
        "/Sneha.jpg"
    ];

    const [currentImage, setCurrentImage] = useState(0);

    useEffect(() => {
        const timer = setInterval(() => {
            setCurrentImage((previousImage) => {
                return (previousImage + 1) % aboutImages.length;
            });
        }, 3000);

        return () => clearInterval(timer);
    }, []);

    return (
        <>
            <section className="hero-section">
                <div className="hero-video">
                    <video
                        autoPlay
                        muted
                        loop
                        playsInline
                        className="hero-video__element"
                        preload="metadata"
                    >
                        <source src="/Bg.mp4" type="video/mp4" />
                    </video>

                    <div className="hero-overlay">
                        <div className="hero-content">
                            <h1 className="hero-title">
                                Welcome to MediCurex
                            </h1>

                            <p className="hero-description">
                                MediCurex is a digital healthcare platform that
                                connects patients and doctors on a single secure
                                interface. It allows users to book appointments,
                                consult doctors online, manage medical records,
                                and access medicines easily, ensuring fast,
                                reliable, and technology-driven healthcare
                                services.
                            </p>
                        </div>
                    </div>
                </div>
            </section>

            <section className="about-section">
                <h2 className="about-title">
                    About Us
                </h2>

                <div className="about-container">
                    <p className="about-description">
                        At MediCurex, we are dedicated to revolutionizing
                        healthcare by leveraging technology to provide seamless
                        access to medical services. Our platform is designed to
                        bridge the gap between patients and healthcare providers,
                        ensuring that quality care is just a click away. Whether
                        you need to consult with a specialist, manage your health
                        records, or order medications, MediCurex is here to make
                        healthcare more accessible and efficient for everyone.
                    </p>

                    <img
                        key={currentImage}
                        src={aboutImages[currentImage]}
                        alt="About MediCurex"
                        className="about-image"
                    />
                </div>
            </section>

            <Services />

            <footer className="site-footer">
                <p className="footer-text">
                    &copy; 2025 MediCurex. All rights reserved.
                </p>
            </footer>
        </>
    );
};

export default Home;