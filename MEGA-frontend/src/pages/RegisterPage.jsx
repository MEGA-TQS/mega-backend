import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const RegisterPage = () => {
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
        confirmPassword: '',
        role: 'USER', // Default role for new sign-ups
    });
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        
        if (formData.password !== formData.confirmPassword) {
            alert("Error: Passwords do not match!");
            return; // Stop the function from proceeding
        }

        // --- SIMULATION LOGIC ---
        
        // In a real application, you would send a POST request here:
        // await api.post('/register', formData);

        // For the MVP, we simulate successful registration and immediate login
        const newUser = {
            // NOTE: Use a unique ID (based on the current timestamp for simplicity)
            id: Date.now(), 
            name: formData.name,
            email: formData.email,
            role: formData.role, 
        };

        login(newUser);
        alert(`Registration successful! Logged in as ${newUser.name} (${newUser.role})`);
        
        // Redirect based on role
        navigate(newUser.role === 'ADMIN' ? '/owner-dashboard' : '/');
    };

    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-md-5">
                    <div className="card shadow-sm border-0">
                        <div className="card-body p-4">
                            <h3 className="text-center mb-4 fw-bold">Create Account</h3>
                            
                            <form onSubmit={handleSubmit}>
                                <div className="mb-3">
                                    <label className="form-label">Full Name</label>
                                    <input 
                                        type="text" 
                                        className="form-control" 
                                        name="name"
                                        value={formData.name}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">Email</label>
                                    <input 
                                        type="email" 
                                        className="form-control" 
                                        name="email"
                                        value={formData.email}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">Password</label>
                                    <input 
                                        type="password" 
                                        className="form-control" 
                                        name="password"
                                        value={formData.password}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">Confirm Password</label>
                                    <input 
                                        type="password" 
                                        className="form-control" 
                                        name="confirmPassword"
                                        value={formData.confirmPassword}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                                
                                {/* Development Tool: Role Selector */}
                                <div className="mb-3">
                                    <label className="form-label">Account Type (for Testing)</label>
                                    <select 
                                        className="form-select" 
                                        name="role"
                                        value={formData.role}
                                        onChange={handleChange}
                                    >
                                        <option value="USER">User</option>
                                        <option value="ADMIN">Admin</option>
                                    </select>
                                </div>

                                <button type="submit" className="btn btn-success w-100 mb-3">Register</button>
                            </form>
                            
                            <div className="text-center mt-3 pt-3 border-top">
                                <p className="small">Already have an account? <Link to="/login">Login here</Link></p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default RegisterPage;