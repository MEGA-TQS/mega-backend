import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import ItemService from '../services/ItemService';
import { useAuth } from '../context/AuthContext';
import { CATEGORIES } from '../constants/categories';

const ItemFormPage = () => {
    const { id } = useParams(); // If ID exists, we are Editing
    const navigate = useNavigate();
    const { user } = useAuth();
    
    const [formData, setFormData] = useState({
        name: '',
        category: 'Surf', // Default
        pricePerDay: '',
        location: '',
        description: '',
        condition: 'Good',
        ownerId: user?.id 
    });

    // Load data if Editing
    useEffect(() => {
        if (id) {
            ItemService.getItemById(id).then(data => {
                setFormData({
                    ...data,
                    ownerId: user.id // Ensure we keep ownership
                });
            });
        }
    }, [id, user.id]);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (id) {
                // await ItemService.updateItem(id, formData); // You need to implement this in backend later
                alert("Edit feature coming soon! (Backend Update needed)");
            } else {
                await ItemService.createItem({ ...formData, ownerId: user.id });
                alert("Item listed successfully!");
                navigate('/my-listings');
            }
        } catch (error) {
            alert("Error saving item. Check console.");
        }
    };

    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-md-8">
                    <div className="card shadow">
                        <div className="card-header bg-primary text-white">
                            <h4 className="mb-0">{id ? 'Edit Item' : 'List New Gear'}</h4>
                        </div>
                        <div className="card-body">
                            <form onSubmit={handleSubmit}>
                                <div className="row mb-3">
                                    <div className="col-md-6">
                                        <label className="form-label">Item Name</label>
                                        <input name="name" className="form-control" value={formData.name} onChange={handleChange} required />
                                    </div>
                                    <div className="col-md-6">
                                        <label className="form-label">Category</label>
                                        <select 
                                            name="category" 
                                            className="form-select" 
                                            value={formData.category} 
                                            onChange={handleChange}
                                        >
                                            {/* DYNAMIC MAPPING */}
                                            {CATEGORIES.map(cat => (
                                                <option key={cat.name} value={cat.name}>
                                                    {cat.name}
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                </div>

                                <div className="row mb-3">
                                    <div className="col-md-6">
                                        <label className="form-label">Price per Day (€)</label>
                                        <input type="number" name="pricePerDay" className="form-control" value={formData.pricePerDay} onChange={handleChange} required />
                                    </div>
                                    <div className="col-md-6">
                                        <label className="form-label">Location</label>
                                        <input name="location" className="form-control" value={formData.location} onChange={handleChange} required placeholder="e.g. Lisbon" />
                                    </div>
                                </div>

                                <div className="mb-3">
                                    <label className="form-label">Description</label>
                                    <textarea name="description" className="form-control" rows="3" value={formData.description} onChange={handleChange} required></textarea>
                                </div>

                                <div className="d-flex justify-content-end gap-2">
                                    <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>Cancel</button>
                                    <button type="submit" className="btn btn-success">{id ? 'Update Item' : 'Create Listing'}</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ItemFormPage;