import React from 'react';
import { Link } from 'react-router-dom';
import SearchBar from '../components/SearchBar';
import { CATEGORIES } from '../constants/categories'; // Ensure this path is correct

const HomePage = () => {
    return (
        <div className="container mt-4">
            {/* HERO SECTION */}
            <div className="hero-section position-relative mb-5" style={{
                backgroundImage: `linear-gradient(rgba(0,0,0,0.5), rgba(0,0,0,0.5)), url('https://images.unsplash.com/photo-1517649763962-0c623066013b?ixlib=rb-1.2.1&auto=format&fit=crop&w=1950&q=80')`,
                backgroundSize: 'cover',
                backgroundPosition: 'center',
                padding: '5rem 2rem',
                borderRadius: '15px',
                color: 'white',
                textAlign: 'center'
            }}>
                <div className="position-relative z-1">
                    <h1 className="display-4 fw-bold mb-4">Rent Gear. Go Outside.</h1>
                    
                    <div className="row justify-content-center">
                        <div className="col-md-10">
                            <SearchBar />
                        </div>
                    </div>
                </div>
            </div>

            {/* CATEGORY HEADER ROW */}
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h4 className="fw-bold m-0">Browse by Category</h4>
                
                {/* NEW: View All Button */}
                <Link to="/browse" className="btn btn-outline-dark btn-sm fw-bold">
                    View All Categories &rarr;
                </Link>
            </div>

            {/* CATEGORY GRID */}
            <div className="row g-3 mb-5">
                {CATEGORIES.map(cat => (
                    <div key={cat.name} className="col-6 col-md-4 col-lg-3">
                        {/* Clicking this sets the URL to /browse?category=Surf */}
                        <Link 
                            to={`/browse?category=${encodeURIComponent(cat.name)}`} 
                            className="text-decoration-none text-white"
                        >
                            <div className="card border-0 overflow-hidden text-white shadow-sm category-card">
                                <img 
                                    src={cat.img} 
                                    className="card-img" 
                                    alt={cat.name} 
                                    style={{
                                        height: '150px', 
                                        objectFit: 'cover', 
                                        filter: 'brightness(0.7)',
                                        transition: 'transform 0.3s'
                                    }} 
                                />
                                <div className="card-img-overlay d-flex align-items-center justify-content-center">
                                    <h5 className="fw-bold text-center text-shadow">{cat.name}</h5>
                                </div>
                            </div>
                        </Link>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default HomePage;