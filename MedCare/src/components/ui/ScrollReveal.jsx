import React, { useEffect, useRef } from "react";
import "./ScrollReveal.css";

const ScrollReveal = ({
    as: Element = "div",
    children,
    className = "",
    delay = 0,
    ...props
}) => {
    const elementRef = useRef(null);

    useEffect(() => {
        const element = elementRef.current;

        if (!element) return undefined;

        const observer = new IntersectionObserver(
            ([entry]) => {
                element.classList.toggle("is-visible", entry.isIntersecting);
            },
            {
                threshold: 0.18,
                rootMargin: "0px 0px -8% 0px"
            }
        );

        observer.observe(element);

        return () => observer.disconnect();
    }, []);

    return (
        <Element
            ref={elementRef}
            className={`scroll-reveal ${className}`.trim()}
            style={{ "--reveal-delay": `${delay}ms` }}
            {...props}
        >
            {children}
        </Element>
    );
};

export default ScrollReveal;
