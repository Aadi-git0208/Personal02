const CART_KEY = "cart";

export function getCart() {
    try {
        const stored = JSON.parse(localStorage.getItem(CART_KEY)) || [];
        return Array.isArray(stored) ? stored : [];
    } catch {
        return [];
    }
}

export function saveCart(items) {
    localStorage.setItem(CART_KEY, JSON.stringify(items));
    window.dispatchEvent(new Event("cartUpdated"));
}

export function addCartItem(medicine, quantity = 1) {
    const medicineId = medicine.id ?? medicine.medicineId;
    const nextQuantity = Math.max(1, Number(quantity) || 1);
    const cart = getCart();
    const existing = cart.find((item) => String(item.medicineId || item.id) === String(medicineId));

    let updated;
    if (existing) {
        updated = cart.map((item) =>
            String(item.medicineId || item.id) === String(medicineId)
                ? {
                    ...item,
                    medicineId,
                    quantity: item.quantity + nextQuantity,
                    name: medicine.name,
                    unitPrice: medicine.unitPrice ?? medicine.price,
                    imageUrl: medicine.imageUrl || medicine.image,
                    prescriptionRequired: Boolean(medicine.prescriptionRequired),
                    status: medicine.status
                }
                : item
        );
    } else {
        updated = [
            ...cart,
            {
                medicineId,
                id: medicineId,
                name: medicine.name,
                quantity: nextQuantity,
                unitPrice: medicine.unitPrice ?? medicine.price,
                imageUrl: medicine.imageUrl || medicine.image,
                prescriptionRequired: Boolean(medicine.prescriptionRequired),
                status: medicine.status
            }
        ];
    }

    saveCart(updated);
    return updated;
}

export function updateCartQuantity(medicineId, quantity) {
    const nextQuantity = Math.max(1, Number(quantity) || 1);
    const updated = getCart().map((item) =>
        String(item.medicineId || item.id) === String(medicineId)
            ? { ...item, quantity: nextQuantity }
            : item
    );
    saveCart(updated);
    return updated;
}

export function removeCartItem(medicineId) {
    const updated = getCart().filter(
        (item) => String(item.medicineId || item.id) !== String(medicineId)
    );
    saveCart(updated);
    return updated;
}

export function clearCart() {
    saveCart([]);
}

export function createIdempotencyKey() {
    if (typeof crypto !== "undefined" && crypto.randomUUID) {
        return crypto.randomUUID();
    }
    return `checkout-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}
