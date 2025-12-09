import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import ItemService from '../services/ItemService';
import '../App.css'; // Make sure to import the CSS

const HomePage = () => {
    const [items, setItems] = useState([]);

    useEffect(() => {
        ItemService.getAllItems()
            .then(data => setItems(data))
            .catch(err => console.log(err));
    }, []);

    // Helper to get random image based on category (since we don't have real uploads yet)
    const getImage = (category) => {
        if (category === 'Surf') return 'https://images.unsplash.com/photo-1502680390469-be75c86b636f?auto=format&fit=crop&w=500&q=60';
        if (category === 'Cycling') return 'https://images.unsplash.com/photo-1532298229144-0ec0c57515c7?auto=format&fit=crop&w=500&q=60';
        return 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=500&q=60';
    };

    return (
        <div className="container mt-4">
            {/* HERO SECTION */}
            <div className="hero-section">
                <h1 className="display-4 fw-bold">Gear Up for Your Next Adventure</h1>
                <p className="lead">Rent high-quality sports equipment from locals. Surf, Bike, Hike, and more.</p>
                <div className="mt-4">
                    <button className="btn btn-light btn-lg me-2 fw-bold text-dark">Browse All</button>
                    <button className="btn btn-outline-light btn-lg fw-bold">List Your Gear</button>
                </div>
            </div>

            {/* CATALOG GRID */}
            <h3 className="mb-4 fw-bold text-dark">Trending Now</h3>
            <div className="row g-4">
                {items.map(item => (
                    <div key={item.id} className="col-md-4 col-sm-6">
                        <div className="card item-card h-100">
                            <div className="card-img-wrapper">
                                <img 
                                    src={getImage(item.category)} 
                                    className="card-img-top" 
                                    alt={item.name} 
                                />
                                <span className="price-tag">€{item.pricePerDay}/day</span>
                            </div>
                            <div className="card-body d-flex flex-column">
                                <div className="d-flex justify-content-between align-items-center mb-2">
                                    <span className="category-badge">{item.category}</span>
                                    <small className="text-muted">⭐ 4.8 (12)</small>
                                </div>
                                <h5 className="card-title fw-bold mb-1">{item.name}</h5>
                                <p className="text-muted small mb-3 flex-grow-1">
                                    {item.description.length > 60 
                                        ? item.description.substring(0, 60) + '...' 
                                        : item.description}
                                </p>
                                <div className="d-flex align-items-center justify-content-between mt-3">
                                    <small className="text-muted">📍 {item.location}</small>
                                    <Link to={`/items/${item.id}`} className="btn btn-primary-custom w-50 text-center text-decoration-none">
                                        View
                                    </Link>
                                </div>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default HomePage;