import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import HomePage from './pages/HomePage';
import ItemDetailsPage from './pages/ItemDetailsPage';
import OwnerDashboard from './pages/OwnerDashboard';
import MyBookingsPage from './pages/MyBookingsPage';
import MyListingsPage from './pages/MyListingsPage';
import ItemFormPage from './pages/ItemFormPage';
import PaymentPage from './pages/PaymentPage';
import BrowsePage from './pages/BrowsePage';
import RegisterPage from './pages/RegisterPage';
import Navbar from './components/Navbar';
import 'bootstrap/dist/css/bootstrap.min.css';

function App() {
  return (
    <Router>
      <Navbar />
      <Routes>
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<HomePage />} />
        <Route path="/browse" element={<BrowsePage />} />
        <Route path="/items/:id" element={<ItemDetailsPage />} />
        <Route path="/owner" element={<OwnerDashboard />} />
        <Route path="/my-bookings" element={<MyBookingsPage />} />
        <Route path="/my-listings" element={<MyListingsPage />} />
        <Route path="/items/new" element={<ItemFormPage />} />
        <Route path="/items/edit/:id" element={<ItemFormPage />} />
        <Route path="/payment/:bookingId" element={<PaymentPage />} />
      </Routes>
    </Router>
  );
}

export default App;