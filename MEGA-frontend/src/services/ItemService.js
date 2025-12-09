import api from '../api/axiosConfig';

const ItemService = {
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
    }
};

export default ItemService;