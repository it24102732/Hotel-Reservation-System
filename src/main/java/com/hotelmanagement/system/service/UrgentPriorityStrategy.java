package com.hotelmanagement.system.service;

import com.hotelmanagement.system.model.CleaningTask;
import org.springframework.stereotype.Component;

// 2. Concrete Strategy
@Component("URGENT")
public class UrgentPriorityStrategy implements CleaningPriorityStrategy {
    @Override
    public void handle(CleaningTask task) {
        String originalNotes = task.getNotes() != null ? task.getNotes() : "";
        task.setNotes("[URGENT - ATTEND IMMEDIATELY] " + originalNotes);
        task.setStatus("IN_PROGRESS"); // Urgent tasks can be started immediately
        System.out.println("Handling task with URGENT priority for Room: " + task.getRoom().getRoomNumber());
    }
}