package com.sportsgear.rentalplatform.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsgear.rentalplatform.data.Item;
import com.sportsgear.rentalplatform.data.ItemRepository;
import com.sportsgear.rentalplatform.data.User;
import com.sportsgear.rentalplatform.data.UserRepository;
import com.sportsgear.rentalplatform.dto.ItemCreateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ItemControllerIT_Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Tag("US-1")
    void searchItems_Integration_ShouldReturnFilteredItems() throws Exception {
        // Criar dados reais na BD
        User owner = userRepository.save(User.builder().email("owner@test.com").name("Owner").build());
        
        itemRepository.save(Item.builder()
                .name("Surfboard A")
                .category("Water")
                .pricePerDay(BigDecimal.valueOf(20))
                .location("Lisbon")
                .active(true)
                .owner(owner)
                .build());

        itemRepository.save(Item.builder()
                .name("Bike B")
                .category("Land")
                .pricePerDay(BigDecimal.valueOf(15))
                .location("Porto")
                .active(true)
                .owner(owner)
                .build());

        // Chamar a API real
        mockMvc.perform(get("/api/items")
                .param("category", "Water")
                .param("location", "Lisbon")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // Só deve vir 1
                .andExpect(jsonPath("$[0].name", is("Surfboard A")));
    }

    @Test
    @Tag("US-6")
    void createItem_Integration_ShouldPersistInDatabase() throws Exception {
        // Criar Owner
        User owner = userRepository.save(User.builder().email("owner@test.com").name("Owner").build());

        // Criar DTO
        ItemCreateDTO dto = new ItemCreateDTO();
        dto.setName("New Kayak");
        dto.setDescription("Brand new kayak description > 10 chars");
        dto.setCategory("Water");
        dto.setLocation("Faro");
        dto.setPricePerDay(BigDecimal.valueOf(30));
        dto.setCondition("New");
        dto.setOwnerId(owner.getId());

        // POST request
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New Kayak")));

        assert(itemRepository.findAll().size() == 1);
    }
}