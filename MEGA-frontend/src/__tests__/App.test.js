import { render, screen } from '@testing-library/react';
import React from 'react';
import App from './App'; // Assuming your main component is App.jsx

test('renders the main application wrapper', () => {
  render(<App />);
  // Replace this with a specific piece of text or element from your App component
  const element = screen.getByText(/Rent Gear. Go Outside./i); 
  expect(element).toBeInTheDocument();
});