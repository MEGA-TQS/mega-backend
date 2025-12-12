import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import BookingService from '../services/BookingService';

const PaymentPage = () => {
    const { bookingId } = useParams();
    const navigate = useNavigate();

    const handleMockPayment = async () => {
        try {
            await BookingService.payBooking(bookingId);
            alert("Payment Successful! Gear is yours.");
            navigate('/my-bookings');
        } catch (error) {
            alert("Payment failed.");
        }
    };

    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-md-6">
                    <div className="card shadow-lg border-0">
                        <div className="card-body p-5 text-center">
                            <h3 className="mb-4">Secure Checkout</h3>
                            <div className="alert alert-info">
                                Booking #{bookingId}
                            </div>
                            
                            <p className="text-muted mb-4">
                                This is a simulation environment. No real money will be charged.
                            </p>

                            {/* Mock Credit Card Form */}
                            <div className="mb-3 text-start">
                                <label className="form-label small text-muted">Card Number</label>
                                <input type="text" className="form-control" placeholder="0000 0000 0000 0000" disabled />
                            </div>
                            
                            <div className="row mb-4 text-start">
                                <div className="col-6">
                                    <label className="form-label small text-muted">Expiry</label>
                                    <input type="text" className="form-control" placeholder="MM/YY" disabled />
                                </div>
                                <div className="col-6">
                                    <label className="form-label small text-muted">CVV</label>
                                    <input type="text" className="form-control" placeholder="123" disabled />
                                </div>
                            </div>

                            <button onClick={handleMockPayment} className="btn btn-dark w-100 py-2 fw-bold">
                                Confirm Payment
                            </button>
                            <button onClick={() => navigate(-1)} className="btn btn-link text-muted mt-2">
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PaymentPage;