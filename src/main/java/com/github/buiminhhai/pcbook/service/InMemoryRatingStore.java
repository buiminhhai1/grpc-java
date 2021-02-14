package com.github.buiminhhai.pcbook.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryRatingStore implements RatingStore {
    private ConcurrentMap<String, Rating> data;

    public InMemoryRatingStore() {
        this.data = new ConcurrentHashMap<>(0);
    }

    @Override
    public Rating Add(String laptopID, double score) {
        return data.merge(laptopID, new Rating(1, score), Rating::add);
    }
}
