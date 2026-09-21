import React from "react";

const Cart = () => {
  const cart = JSON.parse(localStorage.getItem("cart")) || [];

  const total = cart.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0
  );

  return (
    <div style={{ padding: "40px", color: "#fff" }}>
      <h1 className="cart-title">Your Cart</h1>

      {cart.length === 0 ? (
        <p>Cart is empty</p>
      ) : (
        <>
          {cart.map((item) => (
            <p key={item.id}>
              {item.name} × {item.quantity} = ₹
              {item.price * item.quantity}
            </p>
          ))}

          <h2>Total: ₹{total}</h2>

          <button
            style={{ padding: "10px 20px", marginTop: "20px" }}
            onClick={() => alert("Order placed successfully ✅")}
          >
            Buy Now
          </button>
        </>
      )}
    </div>
  );
};

export default Cart;
