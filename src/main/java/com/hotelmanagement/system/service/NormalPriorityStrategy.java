package com.hotelmanagement.system.service;

import com.hotelmanagement.system.model.CleaningTask;
import org.springframework.stereotype.Component;

// 2. Concrete Strategy
@Component("NORMAL")
public class NormalPriorityStrategy implements CleaningPriorityStrategy {
    @Override
    public void handle(CleaningTask task) {
        // Normal priority tasks require no special handling
        System.out.println("Handling task with Normal priority for Room: " + task.getRoom().getRoomNumber());
    }
}