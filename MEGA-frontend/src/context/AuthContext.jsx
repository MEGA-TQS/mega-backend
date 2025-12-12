import React, { createContext, useState, useContext, useEffect } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    // Check localStorage on load so login persists if you refresh the page
    const [user, setUser] = useState(() => {
        const savedUser = localStorage.getItem('user');
        return savedUser ? JSON.parse(savedUser) : null;
    });

    const login = (userData) => {
        // userData example: { id: 1, name: "Alice", role: "RENTER" }
        setUser(userData);
        localStorage.setItem('user', JSON.stringify(userData));
    };

    const logout = () => {
        setUser(null);
        localStorage.removeItem('user');
    };

    return (
        <AuthContext.Provider value={{ user, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);