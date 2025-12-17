package com.sportsgear.rentalplatform.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsgear.rentalplatform.data.Item;
import com.sportsgear.rentalplatform.dto.BlockDateDTO;
import com.sportsgear.rentalplatform.dto.ItemCreateDTO;
import com.sportsgear.rentalplatform.dto.ItemPriceUpdateDTO;
import com.sportsgear.rentalplatform.service.ItemService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*; 
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean 
    private ItemService itemService;
    
    @Autowired private ObjectMapper objectMapper;

    @Test
    @Tag("US-1")
    public void whenSearchWithAllFilters_thenReturnJsonArray() throws Exception {
        Item surfboard = Item.builder()
                .id(1L)
                .name("Prancha XP")
                .category("Surf")
                .location("Lisboa")
                .pricePerDay(new BigDecimal("25.00"))
                .active(true)
                .build();

        List<Item> allItems = Collections.singletonList(surfboard);

        // Configurar Mock para aceitar os novos argumentos de Data e Preço
        given(itemService.search(any(), any(), any(), any(), any(), any(), any()))
                .willReturn(allItems);

        mockMvc.perform(get("/api/items")
                .param("keyword", "Prancha")
                .param("category", "Surf")
                .param("location", "Lisboa")
                .param("minPrice", "10") // Novos filtros US1
                .param("maxPrice", "50")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-05")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Prancha XP")));

        // Verificar se o serviço foi chamado (com qualquer argumento, simplificado)
        verify(itemService).search(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @Tag("US-2")
    public void whenGetItemDetails_thenReturnFullJson() throws Exception {
        // GIVEN
        Item fullItem = Item.builder()
                .id(99L)
                .name("Kite Surf Set")
                .description("Complete set")
                .technicalSpecs("Size 12m, Bar included") // AC: specs
                .pickupRules("Pickup at Costa da Caparica center") // AC: pickup
                .pricePerDay(new BigDecimal("50.00"))
                .build();

        given(itemService.getItemById(99L)).willReturn(Optional.of(fullItem));

        // WHEN & THEN
        mockMvc.perform(get("/api/items/99")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Kite Surf Set")))
                .andExpect(jsonPath("$.technicalSpecs", is("Size 12m, Bar included"))) // Valida Specs
                .andExpect(jsonPath("$.pickupRules", is("Pickup at Costa da Caparica center"))); // Valida Pickup
                
        verify(itemService).getItemById(99L);
    }

    @Test
    @Tag("US-2")
    public void whenItemNotFound_thenReturn404() throws Exception {
        given(itemService.getItemById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/items/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Tag("US-6")
    public void whenCreateItemValid_thenReturn201() throws Exception {
        // GIVEN
        ItemCreateDTO dto = new ItemCreateDTO();
        dto.setName("New Kayak");
        dto.setDescription("Brand new kayak, never used");
        dto.setCategory("Water Sports");
        dto.setLocation("Faro");
        dto.setPricePerDay(new BigDecimal("30.00"));
        dto.setCondition("New");
        dto.setOwnerId(1L);

        Item savedItem = Item.builder().id(10L).name("New Kayak").build();
        
        given(itemService.createItem(any(ItemCreateDTO.class))).willReturn(savedItem);

        // WHEN & THEN
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.name", is("New Kayak")));
    }

    @Test
    @Tag("US-6")
    public void whenCreateItemInvalidPrice_thenReturn400() throws Exception {
        // GIVEN
        ItemCreateDTO dto = new ItemCreateDTO();
        dto.setName("Bad Item");
        dto.setPricePerDay(new BigDecimal("-10.00")); // PREÇO NEGATIVO (Inválido)

        // WHEN & THEN
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // O @Valid apanha isto
    }

    @Test
    public void whenCreateItemOwnerNotFound_thenReturn400() throws Exception {
        ItemCreateDTO dto = new ItemCreateDTO();
        dto.setName("Kayak");
        dto.setPricePerDay(new BigDecimal("10.00"));
        dto.setOwnerId(999L); // ID que não existe

        given(itemService.createItem(any())).willThrow(new IllegalArgumentException("Owner not found"));

        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Tag("US-7")
    public void whenOwnerUpdatesPrice_thenReturn200() throws Exception {
        ItemPriceUpdateDTO dto = new ItemPriceUpdateDTO();
        dto.setNewPrice(new BigDecimal("35.00"));

        Item updatedItem = Item.builder().id(1L).pricePerDay(new BigDecimal("35.00")).build();
        
        given(itemService.updatePrice(eq(1L), any(), eq(1L))).willReturn(updatedItem);

        mockMvc.perform(patch("/api/items/1/price")
                .param("ownerId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pricePerDay", is(35.00)));
    }

    @Test
    public void whenUpdatePriceNotOwner_thenReturn403() throws Exception {
        ItemPriceUpdateDTO dto = new ItemPriceUpdateDTO();
        dto.setNewPrice(new BigDecimal("50.00"));

        // Service throws IllegalStateException when user is not the owner
        given(itemService.updatePrice(eq(1L), any(), eq(2L)))
            .willThrow(new IllegalStateException("User is not the owner"));

        mockMvc.perform(patch("/api/items/1/price")
                .param("ownerId", "2") // Wrong owner
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    public void whenUpdatePriceItemNotFound_thenReturn404() throws Exception {
        ItemPriceUpdateDTO dto = new ItemPriceUpdateDTO();
        dto.setNewPrice(new BigDecimal("50.00"));

        given(itemService.updatePrice(eq(999L), any(), eq(1L)))
            .willThrow(new IllegalArgumentException("Item not found"));

        mockMvc.perform(patch("/api/items/999/price")
                .param("ownerId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Tag("US-7")
    public void whenBlockDates_thenReturn200() throws Exception {
        BlockDateDTO dto = new BlockDateDTO();
        dto.setStartDate(LocalDate.now().plusDays(5));
        dto.setEndDate(LocalDate.now().plusDays(7));

        // void method just returns normally
        
        mockMvc.perform(post("/api/items/1/block")
                .param("ownerId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
                
        verify(itemService).blockDates(eq(1L), any(), any(), eq(1L));
    }

    @Test
    public void whenBlockDatesConflict_thenReturn400() throws Exception {
        BlockDateDTO dto = new BlockDateDTO();
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(1));

        BDDMockito.willThrow(new IllegalStateException("Dates already occupied"))
            .given(itemService).blockDates(eq(1L), any(), any(), eq(1L));

        mockMvc.perform(post("/api/items/1/block")
                .param("ownerId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenDeleteItem_thenReturn204() throws Exception {
        Long itemId = 1L;
        
        // Mock the service call
        doNothing().when(itemService).deleteItem(itemId);

        // Perform DELETE request
        mockMvc.perform(delete("/api/items/{id}", itemId))
                .andExpect(status().isNoContent()); // Expect 204 No Content
    }

    @Test
    @Tag("US-11")
    public void whenItemIsSeasonallyBlocked_thenItShouldNotAppearInSearch() throws Exception {
        
        // GIVEN: Serviço devolve lista vazia porque o item está bloqueado naquelas datas
        given(itemService.search(any(), any(), any(), any(), any(), any(), any()))
                .willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/items")
                .param("startDate", "2025-12-01") // Início da época bloqueada
                .param("endDate", "2025-12-05")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty()); // Lista vazia
    }

    @Test
    public void whenGetItemsByOwner_thenReturnList() throws Exception {
        Item item1 = Item.builder().id(1L).name("Bike").build();
        Item item2 = Item.builder().id(2L).name("Surfboard").build();
        
        given(itemService.getItemsByOwner(1L)).willReturn(List.of(item1, item2));

        mockMvc.perform(get("/api/items/owner/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].name", is("Bike")));
    }

    @Test
    public void whenAddReview_thenReturnReview() throws Exception {
        com.sportsgear.rentalplatform.dto.ReviewDTO reviewDTO = new com.sportsgear.rentalplatform.dto.ReviewDTO();
        reviewDTO.setRating(5);
        reviewDTO.setComment("Great gear!");

        com.sportsgear.rentalplatform.data.Review savedReview = new com.sportsgear.rentalplatform.data.Review();
        savedReview.setId(1L);
        savedReview.setRating(5);
        savedReview.setComment("Great gear!");

        given(itemService.addReview(eq(1L), any())).willReturn(savedReview);

        mockMvc.perform(post("/api/items/1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment", is("Great gear!")));
    }

    @Test
    public void whenAddReviewToInvalidItem_thenReturn404() throws Exception {
        com.sportsgear.rentalplatform.dto.ReviewDTO reviewDTO = new com.sportsgear.rentalplatform.dto.ReviewDTO();
        // Assume service throws exception when item doesn't exist
        given(itemService.addReview(eq(999L), any())).willThrow(new IllegalArgumentException("Item not found"));

        mockMvc.perform(post("/api/items/999/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isNotFound());
    }
}