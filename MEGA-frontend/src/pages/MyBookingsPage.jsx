import React, { useEffect, useState } from 'react';
import BookingService from '../services/BookingService';

const MyBookingsPage = () => {
    const [bookings, setBookings] = useState([]);
    const CURRENT_RENTER_ID = 1;

    useEffect(() => {
        BookingService.getRenterBookings(CURRENT_RENTER_ID)
            .then(data => setBookings(data));
    }, []);

    const handlePayment = (bookingId) => {
        BookingService.payBooking(bookingId)
            .then(() => {
                alert("Payment Successful! (US5)");
                // Refresh list to show PAID status
                BookingService.getRenterBookings(CURRENT_RENTER_ID).then(data => setBookings(data));
            });
    };

    return (
        <div className="container mt-4">
            <h2>My Bookings (US5)</h2>
            <div className="row">
                {bookings.map(booking => (
                    <div key={booking.id} className="col-12 mb-3">
                        <div className="card">
                            <div className="card-body">
                                <h5 className="card-title">Booking #{booking.id}</h5>
                                <p>Total Price: <strong>€{booking.totalPrice}</strong></p>
                                <p>Status: 
                                    <span className={`badge ms-2 bg-${booking.status === 'APPROVED' ? 'success' : booking.status === 'PAID' ? 'primary' : 'secondary'}`}>
                                        {booking.status}
                                    </span>
                                </p>

                                {/* US5: Only show Pay button if Approved */}
                                {booking.status === 'APPROVED' && (
                                    <button className="btn btn-dark" onClick={() => handlePayment(booking.id)}>
                                        Pay Now via Stripe (Sandbox)
                                    </button>
                                )}
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default MyBookingsPage;