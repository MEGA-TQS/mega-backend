import React, { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import ItemService from '../services/ItemService';
import { CATEGORIES } from '../constants/categories'; 

const BrowsePage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [items, setItems] = useState([]);
    
    const [filters, setFilters] = useState({
        keyword: searchParams.get('keyword') || '',
        location: searchParams.get('location') || '',
        category: searchParams.get('category') || '',
        minPrice: '',
        maxPrice: ''
    });

    useEffect(() => {
        const fetchItems = async () => {
            const data = await ItemService.searchItems(filters);
            setItems(data);
        };
        fetchItems();
    }, [filters]);

    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({ ...prev, [name]: value }));
    };

    // Robust Image Finder
    const getImage = (category) => {
        // We use .find() to match the item category to our list of images
        const found = CATEGORIES.find(c => c.name === category);
        // If found, return image. If not, return "Other" image or a fallback.
        return found ? found.img : 'https://images.unsplash.com/photo-1461896836934-ffe607ba8211?auto=format&fit=crop&w=500&q=60';
    };

    return (
        <div className="container mt-4">
            <div className="row">
                {/* SIDEBAR FILTERS */}
                <div className="col-md-3 mb-4">
                    <div className="card shadow-sm border-0 sticky-top" style={{ top: '80px', zIndex: 1 }}>
                        <div className="card-body">
                            <h5 className="fw-bold mb-3">Filter Results</h5>
                            
                            {/* UPDATED LABEL HERE: "Search" instead of "Keyword" */}
                            <div className="mb-3">
                                <label className="form-label small text-muted fw-bold">Search</label>
                                <input 
                                    type="text" 
                                    className="form-control" 
                                    name="keyword" 
                                    value={filters.keyword} 
                                    onChange={handleFilterChange}
                                    placeholder="e.g. Surfboard"
                                />
                            </div>

                            <div className="mb-3">
                                <label className="form-label small text-muted">Category</label>
                                <select 
                                    className="form-select" 
                                    name="category" 
                                    value={filters.category}
                                    onChange={handleFilterChange}
                                >
                                    <option value="">All Categories</option>
                                    {CATEGORIES.map(cat => (
                                        <option key={cat.name} value={cat.name}>{cat.name}</option>
                                    ))}
                                </select>
                            </div>

                            {/* ... Rest of sidebar (Location, Price, Buttons) ... */}
                            <div className="mb-3">
                                <label className="form-label small text-muted">Location</label>
                                <input 
                                    type="text" 
                                    className="form-control" 
                                    name="location" 
                                    value={filters.location} 
                                    onChange={handleFilterChange}
                                    placeholder="City..."
                                />
                            </div>

                            <label className="form-label small text-muted">Price Range (€)</label>
                            <div className="d-flex gap-2 mb-3">
                                <input 
                                    type="number" 
                                    className="form-control" 
                                    name="minPrice" 
                                    placeholder="Min" 
                                    value={filters.minPrice}
                                    onChange={handleFilterChange}
                                />
                                <input 
                                    type="number" 
                                    className="form-control" 
                                    name="maxPrice" 
                                    placeholder="Max" 
                                    value={filters.maxPrice}
                                    onChange={handleFilterChange}
                                />
                            </div>

                            <button 
                                className="btn btn-outline-secondary w-100 btn-sm"
                                onClick={() => setFilters({ keyword: '', location: '', category: '', minPrice: '', maxPrice: '' })}
                            >
                                Clear Filters
                            </button>
                        </div>
                    </div>
                </div>

                {/* RESULTS GRID */}
                <div className="col-md-9">
                    <div className="d-flex justify-content-between align-items-center mb-3">
                        <h4 className="m-0">
                            {items.length} {items.length === 1 ? 'Result' : 'Results'} 
                            {filters.keyword && <span> for "{filters.keyword}"</span>}
                        </h4>
                    </div>
                    
                    {items.length === 0 ? (
                        <div className="alert alert-light text-center py-5">
                            <h4>No items found 🔍</h4>
                            <p>Try adjusting your filters or location.</p>
                        </div>
                    ) : (
                        <div className="row g-4">
                            {items.map(item => (
                                <div key={item.id} className="col-md-4 col-sm-6">
                                    <div className="card h-100 shadow-sm border-0 item-card">
                                        <div className="card-img-wrapper" style={{height: '180px', overflow: 'hidden', position: 'relative'}}>
                                            <img 
                                                src={getImage(item.category)} 
                                                className="card-img-top" 
                                                alt={item.name} 
                                                style={{objectFit: 'cover', height: '100%', width: '100%'}}
                                            />
                                            <div className="position-absolute bottom-0 end-0 m-2">
                                                <span className="badge bg-white text-dark shadow-sm">€{item.pricePerDay}/day</span>
                                            </div>
                                        </div>
                                        <div className="card-body">
                                            <div className="mb-1">
                                                <span className="badge bg-light text-secondary border">{item.category}</span>
                                            </div>
                                            <h6 className="card-title fw-bold text-truncate mb-1">{item.name}</h6>
                                            <p className="small text-muted mb-3">📍 {item.location}</p>
                                            <Link to={`/items/${item.id}`} className="btn btn-primary btn-sm w-100 stretched-link">
                                                View Details
                                            </Link>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default BrowsePage;