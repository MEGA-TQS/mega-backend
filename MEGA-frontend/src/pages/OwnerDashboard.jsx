import React, { useEffect, useState } from 'react';
import BookingService from '../services/BookingService';

const OwnerDashboard = () => {
    const [requests, setRequests] = useState([]);

    // HARDCODED OWNER ID for testing US4
    const CURRENT_OWNER_ID = 5; 

    const loadRequests = () => {
        BookingService.getOwnerBookings(CURRENT_OWNER_ID)
            .then(data => setRequests(data))
            .catch(err => console.error(err));
    };

    useEffect(() => {
        loadRequests();
    }, []);

    const handleStatus = (bookingId, isApproved) => {
        BookingService.updateStatus(bookingId, CURRENT_OWNER_ID, isApproved)
            .then(() => {
                loadRequests(); // Refresh list
            });
    };

    return (
        <div className="container mt-4">
            <h2>Owner Dashboard</h2>
            <div className="list-group mt-3">
                {requests.map(booking => (
                    <div key={booking.id} className="list-group-item d-flex justify-content-between align-items-center">
                        <div>
                            <h5>Booking #{booking.id}</h5>
                            <p className="mb-1">Dates: {booking.startDate} to {booking.endDate}</p>
                            <p>Status: <span className={`badge bg-${booking.status === 'PENDING' ? 'warning' : booking.status === 'APPROVED' ? 'success' : 'secondary'}`}>{booking.status}</span></p>
                        </div>
                        
                        {booking.status === 'PENDING' && (
                            <div>
                                <button className="btn btn-success me-2" onClick={() => handleStatus(booking.id, true)}>Accept</button>
                                <button className="btn btn-outline-danger" onClick={() => handleStatus(booking.id, false)}>Decline</button>
                            </div>
                        )}
                    </div>
                ))}
                {requests.length === 0 && <p>No booking requests found.</p>}
            </div>
        </div>
    );
};

export default OwnerDashboard;