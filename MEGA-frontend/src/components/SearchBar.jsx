import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const SearchBar = () => {
    const [keyword, setKeyword] = useState('');
    const [location, setLocation] = useState('');
    const navigate = useNavigate();

    const handleSearch = (e) => {
        e.preventDefault();
        // Redirect to /browse with query params
        navigate(`/browse?keyword=${keyword}&location=${location}`);
    };

    return (
        <div className="card p-4 shadow-lg border-0" style={{ borderRadius: '15px' }}>
            <form onSubmit={handleSearch} className="row g-2">
                <div className="col-md-5">
                    <div className="form-floating">
                        <input 
                            type="text" 
                            className="form-control border-0 bg-light" 
                            id="what"
                            placeholder="Surfboard, Bike..."
                            value={keyword}
                            onChange={(e) => setKeyword(e.target.value)}
                        />
                        <label htmlFor="what">What are you looking for?</label>
                    </div>
                </div>
                <div className="col-md-5">
                    <div className="form-floating">
                        <input 
                            type="text" 
                            className="form-control border-0 bg-light" 
                            id="where" 
                            placeholder="Lisbon, Porto..."
                            value={location}
                            onChange={(e) => setLocation(e.target.value)}
                        />
                        <label htmlFor="where">Where?</label>
                    </div>
                </div>
                <div className="col-md-2 d-grid">
                    <button type="submit" className="btn btn-primary fw-bold fs-5">
                        Search
                    </button>
                </div>
            </form>
        </div>
    );
};

export default SearchBar;