import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import ItemService from '../services/ItemService';
import { useAuth } from '../context/AuthContext';

const MyListingsPage = () => {
    const [items, setItems] = useState([]);
    const { user } = useAuth();

    useEffect(() => {
        if (user) {
            ItemService.getMyListings(user.id)
                .then(data => setItems(data))
                .catch(err => console.error(err));
        }
    }, [user]);

    const handleDelete = async (itemId) => {
        if (window.confirm("Are you sure you want to delete this listing?")) {
            await ItemService.deleteItem(itemId);
            // Remove item from UI immediately without refreshing
            setItems(items.filter(item => item.id !== itemId));
        }
    };

    return (
        <div className="container mt-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2>My Listings</h2>
                <Link to="/items/new" className="btn btn-success">+ List New Item</Link>
            </div>

            {items.length === 0 ? (
                <div className="alert alert-info">You haven't listed any items yet.</div>
            ) : (
                <div className="row g-4">
                    {items.map(item => (
                        <div key={item.id} className="col-md-4">
                            <div className="card h-100 shadow-sm">
                                <div className="card-body">
                                    <h5 className="card-title">{item.name}</h5>
                                    <span className="badge bg-primary mb-2">{item.category}</span>
                                    <p className="card-text text-muted">
                                        Price: <strong>€{item.pricePerDay}/day</strong>
                                    </p>
                                    <p className="small text-muted">{item.location}</p>
                                </div>
                                <div className="card-footer bg-white border-top-0 d-flex justify-content-between">
                                    <Link to={`/items/${item.id}`} className="btn btn-outline-primary btn-sm">
                                        View
                                    </Link>
                                    <button 
                                        className="btn btn-outline-danger btn-sm"
                                        onClick={() => handleDelete(item.id)}
                                    >
                                        Delete
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default MyListingsPage;