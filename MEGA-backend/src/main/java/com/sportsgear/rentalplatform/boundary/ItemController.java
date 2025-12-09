package com.sportsgear.rentalplatform.boundary;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sportsgear.rentalplatform.data.Item;
import com.sportsgear.rentalplatform.service.ItemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public List<Item> searchItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location) {

        return itemService.search(keyword, category, location);
    }
}
