import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ItemService from '../services/ItemService';
import BookingService from '../services/BookingService';

const ItemDetailsPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [item, setItem] = useState(null);
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');

    // HARDCODED ID for testing US3
    const CURRENT_USER_ID = 1; 

    useEffect(() => {
        // Fetch item details (US2)
        ItemService.getAllItems().then(items => {
            const found = items.find(i => i.id === parseInt(id));
            setItem(found);
        });
    }, [id]);

    const handleRequest = async () => {
        try {
            const bookingDTO = {
                renterId: CURRENT_USER_ID,
                itemIds: [item.id], // Group booking ready (US10)
                startDate: startDate,
                endDate: endDate
            };
            await BookingService.createBooking(bookingDTO);
            alert("Request sent! Waiting for owner approval.");
            navigate('/my-bookings');
        } catch {
            alert("Error creating booking");
        }
    };

    if (!item) return <div>Loading...</div>;

    return (
        <div className="container mt-5">
            <div className="row">
                <div className="col-md-6">
                    <img src="https://via.placeholder.com/600x400" className="img-fluid rounded" alt={item.name} />
                </div>
                <div className="col-md-6">
                    <h2>{item.name}</h2>
                    <h4 className="text-primary">€{item.pricePerDay} / day</h4>
                    <p className="text-muted">{item.description}</p>
                    
                    <div className="card p-3 mt-4 bg-light">
                        <h5>Request Booking (US3)</h5>
                        <div className="mb-3">
                            <label>Start Date</label>
                            <input type="date" className="form-control" onChange={e => setStartDate(e.target.value)} />
                        </div>
                        <div className="mb-3">
                            <label>End Date</label>
                            <input type="date" className="form-control" onChange={e => setEndDate(e.target.value)} />
                        </div>
                        <button className="btn btn-primary w-100" onClick={handleRequest}>
                            Send Request
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ItemDetailsPage;