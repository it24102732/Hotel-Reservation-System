package com.hotelmanagement.system.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cleaning_tasks")
public class CleaningTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- FIX: Change the relationship from @OneToOne to @ManyToOne ---
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    private String assignedTo;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDate scheduledFor;
    private String priority;

    // Getters and Setters for all fields...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDate getScheduledFor() { return scheduledFor; }
    public void setScheduledFor(LocalDate scheduledFor) { this.scheduledFor = scheduledFor; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}