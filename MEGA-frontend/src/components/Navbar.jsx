import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navbar = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <nav className="navbar navbar-expand-lg navbar-custom sticky-top">
            <div className="container">
                {/* --- UPDATED LOGO AREA --- */}
                <Link className="navbar-brand d-flex align-items-center" to="/">
                    <img 
                        src="/MEGA_completely_original_logo.png" // Use the path to your saved file
                        alt="MEGA Logo"
                        style={{ height: '35px', marginRight: '5px' }} // Custom sizing
                    />
                     <img 
                        src="/Title_image.png" // Use the path to your saved file
                        alt="MEGA Logo"
                        style={{ height: '32px', marginRight: '2px' }} // Custom sizing
                    />
                </Link>
                {/* ------------------------- */}
                
                <div className="d-flex align-items-center gap-3">
                    {user ? (
                        <>
                            {/* ... existing links (My Listings, My Bookings, Dashboard) ... */}

                            {user.role === 'USER' && (
                                <>
                                    <Link to="/my-listings" className="btn btn-outline-dark btn-sm">My Listings</Link>
                                    <Link to="/my-bookings" className="btn btn-light btn-sm">My Bookings</Link>
                                </>
                            )}
                            
                            {user.role === 'ADMIN' && (
                                <Link to="/owner-dashboard" className="btn btn-warning btn-sm text-white">Admin Panel</Link>
                            )}

                            <span className="text-muted d-none d-md-block ms-2">
                                Hi, {user.name.split(' ')[0]}
                            </span>
                            <button onClick={handleLogout} className="btn btn-outline-danger btn-sm">Logout</button>
                        </>
                    ) : (
                        <Link to="/login" className="btn btn-primary btn-sm px-4">Login</Link>
                    )}
                </div>
            </div>
        </nav>
    );
};

export default Navbar;