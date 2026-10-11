import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { pageContent } from "../../api/client";
import { listAdminOrders } from "../../api/orderApi";
import { listMedicines } from "../../api/pharmacyApi";
import { getAdminDirectory } from "../../api/profileApi";
import { useAuth } from "../../auth/AuthContext";
import "./Admin.css";

const AdminDashboard = () => {
    const { user } = useAuth();
    const name = user?.name || "Administrator";
    const [userCount, setUserCount] = useState(0);
    const [medicineCount, setMedicineCount] = useState(0);
    const [orderCount, setOrderCount] = useState(0);

    useEffect(() => {
        async function load() {
            try {
                const [directory, medicines, orders] = await Promise.all([
                    getAdminDirectory(),
                    listMedicines({ size: 1 }),
                    listAdminOrders({ size: 1 })
                ]);
                setUserCount((directory?.patients?.length || 0) + (directory?.doctors?.length || 0));
                setMedicineCount(medicines?.totalElements || pageContent(medicines).length);
                setOrderCount(orders?.totalElements || pageContent(orders).length);
            } catch {
                setUserCount(0);
                setMedicineCount(0);
                setOrderCount(0);
            }
        }
        load();
    }, []);

    return (
        <div className="admin-page">
            <header className="admin-dashboard-header">
                <div>
                    <p className="admin-eyebrow">CONTROL CENTER</p>
                    <h1>{name} <span aria-hidden="true"></span></h1>
                    <p className="admin-subtitle">Keep your MediCurex platform organized and running smoothly.</p>
                </div>
                <div className="admin-header-mark" aria-hidden="true">✦</div>
            </header>
            <div className="admin-stats">
                <div className="admin-stat-card">
                    <span className="admin-stat-kicker">PEOPLE</span>
                    <h3>Profile records</h3>
                    <p>{userCount}</p>
                </div>
                <div className="admin-stat-card">
                    <span className="admin-stat-kicker">CATALOG</span>
                    <h3>Total Medicines</h3>
                    <p>{medicineCount}</p>
                </div>
                <div className="admin-stat-card">
                    <span className="admin-stat-kicker">FULFILLMENT</span>
                    <h3>Total Orders</h3>
                    <p>{orderCount}</p>
                </div>
            </div>
            <div className="admin-links">
                <Link className="admin-link-card" to="/admin/users">
                    <span className="admin-link-index">01</span>
                    <span className="admin-link-copy"><strong>Manage Users</strong><small>Review patient and doctor accounts</small></span>
                    <span className="admin-link-arrow" aria-hidden="true">→</span>
                </Link>
                <Link className="admin-link-card" to="/admin/medicines">
                    <span className="admin-link-index">02</span>
                    <span className="admin-link-copy"><strong>Manage Medicines</strong><small>Maintain the medicine catalog</small></span>
                    <span className="admin-link-arrow" aria-hidden="true">→</span>
                </Link>
                <Link className="admin-link-card" to="/admin/orders">
                    <span className="admin-link-index">03</span>
                    <span className="admin-link-copy"><strong>Orders</strong><small>Monitor pharmacy fulfillment</small></span>
                    <span className="admin-link-arrow" aria-hidden="true">→</span>
                </Link>
                <Link className="admin-link-card" to="/admin/reports">
                    <span className="admin-link-index">04</span>
                    <span className="admin-link-copy"><strong>Reports</strong><small>Not available in the current backend</small></span>
                    <span className="admin-link-arrow" aria-hidden="true">→</span>
                </Link>
            </div>
        </div>
    );
};

export default AdminDashboard;
