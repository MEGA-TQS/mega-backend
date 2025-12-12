import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const LoginPage = () => {
    const [email, setEmail] = useState('');
    const { login } = useAuth();
    const navigate = useNavigate();

    // UPDATED MOCK USERS
    const TEST_USERS = [
        { id: 1, name: "Regular User", email: "user@test.com", role: "USER" },
        { id: 99, name: "Admin User", email: "admin@test.com", role: "ADMIN" }
    ];

    const handleLogin = (e) => {
        e.preventDefault();
        const foundUser = TEST_USERS.find(u => u.email === email);
        
        if (foundUser) {
            login(foundUser);
            // Admins go to dashboard, Users go to home
            navigate(foundUser.role === 'ADMIN' ? '/owner-dashboard' : '/');
        } else {
            alert("User not found! Try 'user@test.com' or 'admin@test.com'");
        }
    };

    const quickLogin = (user) => {
        login(user);
        navigate(user.role === 'ADMIN' ? '/owner-dashboard' : '/');
    };

    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-md-5">
                    <div className="card shadow-sm border-0">
                        <div className="card-body p-4">
                            <h3 className="text-center mb-4 fw-bold">Sign In</h3>
                            
                            <form onSubmit={handleLogin}>
                                <div className="mb-3">
                                    <label className="form-label">Email</label>
                                    <input 
                                        type="email" 
                                        className="form-control" 
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        placeholder="user@test.com"
                                    />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">Password</label>
                                    <input type="password" className="form-control" placeholder="******" />
                                </div>
                                <button type="submit" className="btn btn-primary w-100 mb-3">Login</button>
                            </form>

                            <p className="small mt-3">Don't have an account? <Link to="/register">Register here</Link></p>

                            <div className="text-center mt-3 pt-3 border-top">
                                <p className="text-muted small mb-2">Development Shortcuts</p>
                                <button 
                                    className="btn btn-outline-info btn-sm me-2"
                                    onClick={() => quickLogin(TEST_USERS[0])}
                                >
                                    Login as User
                                </button>
                                <button 
                                    className="btn btn-outline-danger btn-sm"
                                    onClick={() => quickLogin(TEST_USERS[1])}
                                >
                                    Login as Admin
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;