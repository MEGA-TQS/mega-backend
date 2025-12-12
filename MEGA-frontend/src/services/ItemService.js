import api from '../api/axiosConfig';

const ItemService = {

    searchItems: async (filters) => {
        // filters object: { keyword, category, location, minPrice, maxPrice, startDate, endDate }
        
        // Remove empty keys to keep URL clean
        const params = new URLSearchParams();
        Object.keys(filters).forEach(key => {
            if (filters[key]) params.append(key, filters[key]);
        });

        try {
            const response = await api.get(`/items?${params.toString()}`);
            return response.data;
        } catch (error) {
            console.error("Search failed", error);
            return [];
        }
    },

    // Matches GET /api/items in ItemController.java
    getAllItems: async () => {
        try {
            const response = await api.get('/items');
            return response.data;
        } catch (error) {
            console.error("Error fetching items", error);
            throw error;
        }
    },

    // Matches POST /api/items
    createItem: async (itemData) => {
        const response = await api.post('/items', itemData);
        return response.data;
    },

    getMyListings: async (ownerId) => {
        try {
            const response = await api.get(`/items/owner/${ownerId}`);
            return response.data;
        } catch (error) {
            console.error("Error fetching my listings", error);
            throw error;
        }
    }
};

export default ItemService;