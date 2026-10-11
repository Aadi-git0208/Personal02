import { useEffect, useMemo, useState } from "react";
import { getApiErrorMessage, pageContent } from "../api/client";
import {
    clearCart,
    createIdempotencyKey,
    getCart,
    removeCartItem,
    updateCartQuantity
} from "../api/cart";
import { cancelOrder, createOrder, listMyOrders } from "../api/orderApi";
import { useAuth } from "../auth/AuthContext";

const CHECKOUT_KEY = "medicurex.checkoutKey";

function Cart() {
    const { user } = useAuth();
    const [cart, setCart] = useState(() => getCart());
    const [recipientName, setRecipientName] = useState(user?.name || "");
    const [phone, setPhone] = useState("");
    const [address, setAddress] = useState("");
    const [instructions, setInstructions] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [lastOrder, setLastOrder] = useState(null);
    const [orders, setOrders] = useState([]);
    const [idempotencyKey, setIdempotencyKey] = useState(
        () => sessionStorage.getItem(CHECKOUT_KEY) || createIdempotencyKey()
    );

    useEffect(() => {
        sessionStorage.setItem(CHECKOUT_KEY, idempotencyKey);
    }, [idempotencyKey]);

    useEffect(() => {
        const refresh = () => setCart(getCart());
        window.addEventListener("cartUpdated", refresh);
        window.addEventListener("storage", refresh);
        return () => {
            window.removeEventListener("cartUpdated", refresh);
            window.removeEventListener("storage", refresh);
        };
    }, []);

    async function loadOrders() {
        if (user?.role !== "patient") {
            return;
        }
        try {
            const page = await listMyOrders({ size: 20, sort: "createdAt,desc" });
            setOrders(pageContent(page));
        } catch {
            setOrders([]);
        }
    }

    useEffect(() => {
        loadOrders();
    }, [user?.role]);

    const estimatedTotal = useMemo(
        () => cart.reduce((sum, item) => sum + Number(item.unitPrice || item.price || 0) * Number(item.quantity || 0), 0),
        [cart]
    );

    const prescriptionItems = cart.filter((item) => item.prescriptionRequired);

    async function checkout(retry = false) {
        setError("");
        setSuccess("");

        if (!user || user.role !== "patient") {
            setError("Sign in as a patient to place an order.");
            return;
        }

        if (cart.length === 0) {
            setError("Your cart is empty.");
            return;
        }

        if (!recipientName.trim() || !phone.trim() || !address.trim()) {
            setError("Recipient name, phone, and delivery address are required.");
            return;
        }

        const key = retry ? idempotencyKey : idempotencyKey;
        setSubmitting(true);
        try {
            const order = await createOrder({
                items: cart.map((item) => ({
                    medicineId: Number(item.medicineId || item.id),
                    quantity: Number(item.quantity)
                })),
                recipientName: recipientName.trim(),
                phone: phone.trim(),
                address: address.trim(),
                instructions: instructions.trim() || undefined,
                idempotencyKey: key
            }, key);
            setLastOrder(order);
            setSuccess(`Order #${order.id} ${order.status}. Server total: ₹${order.totalAmount}.`);
            clearCart();
            setCart([]);
            const nextKey = createIdempotencyKey();
            setIdempotencyKey(nextKey);
            sessionStorage.setItem(CHECKOUT_KEY, nextKey);
            await loadOrders();
        } catch (checkoutError) {
            setError(getApiErrorMessage(checkoutError));
        } finally {
            setSubmitting(false);
        }
    }

    async function handleCancel(orderId) {
        setError("");
        try {
            await cancelOrder(orderId, "Cancelled by patient");
            await loadOrders();
        } catch (cancelError) {
            setError(getApiErrorMessage(cancelError));
        }
    }

    return (
        <div style={{ padding: "40px", color: "#fff" }}>
            <h1 className="cart-title">Your Cart</h1>
            {error && <p role="alert">{error}</p>}
            {success && <p>{success}</p>}

            {cart.length === 0 ? (
                <p>Cart is empty</p>
            ) : (
                <>
                    {cart.map((item) => (
                        <p key={item.medicineId || item.id}>
                            {item.name} ×
                            <input
                                type="number"
                                min="1"
                                max="100"
                                value={item.quantity}
                                onChange={(event) => setCart(updateCartQuantity(item.medicineId || item.id, event.target.value))}
                                style={{ width: "60px", margin: "0 8px" }}
                            />
                            {item.prescriptionRequired ? " (prescription required)" : ""}
                            <button type="button" onClick={() => setCart(removeCartItem(item.medicineId || item.id))}>
                                Remove
                            </button>
                        </p>
                    ))}
                    <h2>Estimated total: ₹{estimatedTotal.toFixed(2)}</h2>
                    <p>The amount charged is calculated by the server at checkout. Cart prices are for display only.</p>
                    {prescriptionItems.length > 0 && (
                        <p role="alert">
                            Prescription-required medicines cannot be purchased yet. Remove them before checkout, or the order will be rejected.
                        </p>
                    )}
                    <div>
                        <input
                            placeholder="Recipient name"
                            value={recipientName}
                            onChange={(event) => setRecipientName(event.target.value)}
                        />
                        <input
                            placeholder="Phone"
                            value={phone}
                            onChange={(event) => setPhone(event.target.value)}
                        />
                        <input
                            placeholder="Delivery address"
                            value={address}
                            onChange={(event) => setAddress(event.target.value)}
                        />
                        <input
                            placeholder="Delivery instructions (optional)"
                            value={instructions}
                            onChange={(event) => setInstructions(event.target.value)}
                        />
                    </div>
                    <button
                        style={{ padding: "10px 20px", marginTop: "20px" }}
                        disabled={submitting}
                        onClick={() => checkout(false)}
                    >
                        {submitting ? "Placing order..." : "Buy Now"}
                    </button>
                    <button
                        style={{ padding: "10px 20px", marginTop: "20px", marginLeft: "12px" }}
                        disabled={submitting}
                        onClick={() => checkout(true)}
                    >
                        Retry same checkout
                    </button>
                </>
            )}

            {lastOrder && (
                <div>
                    <h2>Latest server total: ₹{lastOrder.totalAmount}</h2>
                    <p>Status: {lastOrder.status}</p>
                </div>
            )}

            {user?.role === "patient" && (
                <div>
                    <h2>Order history</h2>
                    {orders.length === 0 ? (
                        <p>No orders yet.</p>
                    ) : (
                        orders.map((order) => (
                            <div key={order.id}>
                                <p>
                                    #{order.id} · {order.status} · ₹{order.totalAmount}
                                </p>
                                {(order.status === "PENDING" || order.status === "CONFIRMED") && (
                                    <button type="button" onClick={() => handleCancel(order.id)}>
                                        Cancel order
                                    </button>
                                )}
                            </div>
                        ))
                    )}
                </div>
            )}
        </div>
    );
}

export default Cart;
