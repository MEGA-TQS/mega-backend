package com.sportsgear.rentalplatform.boundary;

import com.sportsgear.rentalplatform.data.Item;
import com.sportsgear.rentalplatform.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class) //Foca o teste apenas no Controller
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simula o envio de pedidos HTTP 

    @MockBean
    private ItemService itemService; // Cria um "falso" serviço para não usar a BD

    @Test
    public void whenSearchWithFilters_thenReturnJsonArray() throws Exception {
        Item surfboard = Item.builder()
                .id(1L)
                .name("Prancha XP")
                .category("Surf")
                .location("Lisboa")
                .pricePerDay(new BigDecimal("25.00"))
                .active(true)
                .build();

        List<Item> allItems = Arrays.asList(surfboard);

        given(itemService.search("Prancha", "Surf", "Lisboa")).willReturn(allItems);

        mockMvc.perform(get("/api/items")
                .param("keyword", "Prancha")
                .param("category", "Surf")
                .param("location", "Lisboa")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Espera HTTP 200
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1))) // Espera 1 elemento
                .andExpect(jsonPath("$[0].name", is("Prancha XP"))) // Verifica o nome
                .andExpect(jsonPath("$[0].category", is("Surf"))); // Verifica a categoria

        verify(itemService).search("Prancha", "Surf", "Lisboa");
    }

    @Test
    public void whenSearchReturnsEmpty_thenReturnEmptyJson() throws Exception {
        given(itemService.search(null, "Ski", "Algarve"))
                .willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/items")
                .param("category", "Ski")
                .param("location", "Algarve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }
}
