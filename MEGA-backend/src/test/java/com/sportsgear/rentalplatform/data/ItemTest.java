package com.sportsgear.rentalplatform.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ItemTest {
    @Test
    void getAverageRating_ShouldReturnZero_WhenReviewsIsNull() {

        Item item = new Item();
        item.setReviews(null);

        Double avg = item.getAverageRating();

        assertEquals(0.0, avg);
    }

    @Test
    void getAverageRating_ShouldReturnZero_WhenReviewsIsEmpty() {

        Item item = new Item();
        item.setReviews(new ArrayList<>());

        Double avg = item.getAverageRating();

        assertEquals(0.0, avg);
    }

    @Test
    void getAverageRating_ShouldReturnCorrectAverage_WhenReviewsExist() {

        Review r1 = new Review(); r1.setRating(5);
        Review r2 = new Review(); r2.setRating(3);
        Review r3 = new Review(); r3.setRating(4);

        Item item = Item.builder()
                .reviews(List.of(r1, r2, r3))
                .build();

        Double avg = item.getAverageRating();

        assertEquals(4.0, avg);
    }

    @Test
    void getAverageRating_ShouldHandleDecimalResult() {

        Review r1 = new Review(); r1.setRating(5);
        Review r2 = new Review(); r2.setRating(4);

        Item item = Item.builder()
                .reviews(List.of(r1, r2))
                .build();

        Double avg = item.getAverageRating();

        assertEquals(4.5, avg);
    }

    @Test
    void builder_ShouldConstructItemCorrectly() {

        Item item = Item.builder()
                .id(1L)
                .name("Surfboard")
                .active(true)
                .instantBookable(true)
                .build();

        assertEquals(1L, item.getId());
        assertEquals("Surfboard", item.getName());
        assertTrue(item.isActive());
        assertTrue(item.isInstantBookable());
    }
}
