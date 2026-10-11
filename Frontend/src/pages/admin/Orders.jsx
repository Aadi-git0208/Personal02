import React, { useEffect, useState } from "react";
import { getApiErrorMessage, pageContent } from "../../api/client";
import { listAdminOrders, updateOrderStatus } from "../../api/orderApi";
import "./Admin.css";

const orderStatuses = ["PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED", "REJECTED"];

const Orders = () => {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    async function loadOrders() {
        setLoading(true);
        setError("");
        try {
            const page = await listAdminOrders({ size: 50, sort: "createdAt,desc" });
            setOrders(pageContent(page));
        } catch (loadError) {
            setError(getApiErrorMessage(loadError));
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadOrders();
    }, []);

    async function changeStatus(orderId, status) {
        setError("");
        try {
            await updateOrderStatus(orderId, status);
            await loadOrders();
        } catch (statusError) {
            setError(getApiErrorMessage(statusError));
        }
    }

    return (
        <div className="admin-page">
            <h1>Orders</h1>
            {error && <p role="alert">{error}</p>}
            <div className="admin-section">
                <h2>Order History</h2>
                {loading && <p>Loading orders...</p>}
                {!loading && orders.length === 0 && <p className="admin-empty">No orders found</p>}
                {orders.map((order) => (
                    <div className="admin-row" key={order.id}>
                        <span>
                            #{order.id} · Patient #{order.patientUserId} · {order.status} · ₹{order.totalAmount}
                        </span>
                        <select
                            value={order.status}
                            onChange={(event) => changeStatus(order.id, event.target.value)}
                        >
                            {orderStatuses.map((status) => (
                                <option key={status} value={status}>{status}</option>
                            ))}
                        </select>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default Orders;
