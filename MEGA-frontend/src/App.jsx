import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import HomePage from './pages/HomePage';
import ItemDetailsPage from './pages/ItemDetailsPage';
import OwnerDashboard from './pages/OwnerDashboard';
import MyBookingsPage from './pages/MyBookingsPage';
import 'bootstrap/dist/css/bootstrap.min.css';

function App() {
  return (
    <Router>
      {/* Temporary Nav for Testing */}
      <nav className="navbar navbar-expand navbar-light navbar-custom sticky-top mb-3">
          <div className="container">
              <Link className="navbar-brand d-flex align-items-center" to="/">
                  {/* You can add a logo icon here later */}
                  ⚡ MEGA
              </Link>
              <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                  <span className="navbar-toggler-icon"></span>
              </button>
              <div className="collapse navbar-collapse justify-content-end" id="navbarNav">
                  <div className="navbar-nav gap-3">
                      <Link className="nav-link fw-bold text-dark" to="/">Explore</Link>
                      <Link className="nav-link fw-bold text-dark" to="/my-bookings">My Bookings</Link>
                      <Link className="nav-link fw-bold text-primary" to="/owner">Owner Dashboard</Link>
                  </div>
              </div>
          </div>
      </nav>

      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/items/:id" element={<ItemDetailsPage />} />
        <Route path="/owner" element={<OwnerDashboard />} />
        <Route path="/my-bookings" element={<MyBookingsPage />} />
      </Routes>
    </Router>
  );
}

export default App;