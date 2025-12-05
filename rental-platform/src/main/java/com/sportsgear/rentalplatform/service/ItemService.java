package com.sportsgear.rentalplatform.service;

import java.util.List;

import org.springframework.stereotype.Service; // Certifica-te que importas o package certo

import com.sportsgear.rentalplatform.data.Item;
import com.sportsgear.rentalplatform.data.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public List<Item> search(String keyword, String category, String location) {
        // é aqui que se vai adicionar a lógica de verificar datas

        return itemRepository.searchItems(keyword, category, location);
    }
}
