import api from '../api/axiosConfig';

const BookingService = {
    // Create a booking request
    createBooking: async (bookingData) => {
        // bookingData should match the DTO: { renterId, itemIds: [], startDate, endDate }
        const response = await api.post('/bookings', bookingData);
        return response.data;
    },

    // Owner gets their requests (You need to add this endpoint to Backend later)
    // For now, we mock fetching all and filtering in frontend, or use existing endpoints
    getOwnerBookings: async (ownerId) => {
        // Ideally: api.get(`/bookings/owner/${ownerId}`)
        // For testing US4 without backend filter, we assume backend returns all
        const response = await api.get('/bookings'); 
        return response.data.filter(b => b.ownerId === ownerId);
    },

    // Owner Accepts/Declines
    updateStatus: async (bookingId, ownerId, isApproved) => {
        const response = await api.patch(`/bookings/${bookingId}/status`, null, {
            params: { ownerId, approved: isApproved }
        });
        return response.data;
    },

    // Renter pays
    payBooking: async (bookingId) => {
        const response = await api.post(`/bookings/${bookingId}/pay`);
        return response.data;
    },

    // Helper: Get Renter history
    getRenterBookings: async (renterId) => {
        const response = await api.get('/bookings');
        return response.data.filter(b => b.renterId === renterId); // Simple filter
    }
};

export default BookingService;