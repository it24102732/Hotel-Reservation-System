package com.hotelmanagement.system.service;

import com.hotelmanagement.system.model.CleaningTask;

// 1. Strategy Interface
public interface CleaningPriorityStrategy {
    void handle(CleaningTask task);
}