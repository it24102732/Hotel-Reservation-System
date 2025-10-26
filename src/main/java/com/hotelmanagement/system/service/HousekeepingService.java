package com.hotelmanagement.system.service;

import com.hotelmanagement.system.model.CleaningTask;
import com.hotelmanagement.system.model.Room;
import com.hotelmanagement.system.repository.CleaningTaskRepository;
import com.hotelmanagement.system.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HousekeepingService {

    @Autowired
    private CleaningTaskRepository cleaningTaskRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private Map<String, CleaningPriorityStrategy> priorityStrategies;

    @Transactional
    public CleaningTask createCleaningTask(Long roomId, String assignedTo, String notes, LocalDate scheduledFor, String priority) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));

        room.setAvailable(false);
        roomRepository.save(room);

        CleaningTask task = new CleaningTask();
        task.setRoom(room);
        task.setAssignedTo(assignedTo);
        task.setNotes(notes);
        task.setScheduledFor(scheduledFor);
        task.setPriority(priority);
        task.setCreatedAt(LocalDateTime.now());
        task.setStatus("PENDING");

        CleaningPriorityStrategy strategy = priorityStrategies.get(priority);
        if (strategy != null) {
            strategy.handle(task);
        }

        return cleaningTaskRepository.save(task);
    }

    @Transactional
    public CleaningTask updateTaskStatus(Long taskId, String status) {
        CleaningTask task = cleaningTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Cleaning task not found with ID: " + taskId));

        task.setStatus(status);

        if ("COMPLETED".equalsIgnoreCase(status)) {
            task.setCompletedAt(LocalDateTime.now());
            Room room = task.getRoom();
            room.setAvailable(true);
            roomRepository.save(room);
        }

        return cleaningTaskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        CleaningTask task = cleaningTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Cleaning task not found with ID: " + taskId));

        if (!"COMPLETED".equalsIgnoreCase(task.getStatus())) {
            Room room = task.getRoom();
            room.setAvailable(true);
            roomRepository.save(room);
        }
        cleaningTaskRepository.deleteById(taskId);
    }

    public List<CleaningTask> getAllCleaningTasks() {
        return cleaningTaskRepository.findAll();
    }

    /**
     * THIS IS THE MISSING METHOD.
     * It provides the data for the summary cards on the dashboard.
     */
    public Map<String, Long> getTaskSummary() {
        Map<String, Long> summary = new HashMap<>();
        summary.put("pendingCount", cleaningTaskRepository.countByStatus("PENDING"));
        summary.put("inProgressCount", cleaningTaskRepository.countByStatus("IN_PROGRESS"));
        summary.put("completedTodayCount", cleaningTaskRepository.countCompletedToday());
        // This value is hardcoded for now as user management is separate.
        summary.put("staffOnDuty", 8L);
        return summary;
    }
}