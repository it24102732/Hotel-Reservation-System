package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.model.CleaningTask;
import com.hotelmanagement.system.model.Room;
import com.hotelmanagement.system.repository.RoomRepository;
import com.hotelmanagement.system.service.HousekeepingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/housekeeping")
public class HousekeepingController {

    @Autowired
    private HousekeepingService housekeepingService;
    @Autowired
    private RoomRepository roomRepository;

    // --- REMOVED ---
    // The UserService injection is no longer needed.
    // @Autowired
    // private UserService userService;

    @GetMapping
    public String showHousekeepingPage(Model model) {
        Map<String, Long> summary = housekeepingService.getTaskSummary();
        List<Room> availableRooms = roomRepository.findByIsAvailable(true);
        List<CleaningTask> tasks = housekeepingService.getAllCleaningTasks();

        model.addAttribute("summary", summary);
        model.addAttribute("tasks", tasks);
        model.addAttribute("availableRooms", availableRooms);

        // We no longer need to add housekeepingStaff to the model.

        return "housekeeping";
    }

    @PostMapping("/tasks")
    public String createCleaningTask(@RequestParam Long roomId,
                                     @RequestParam String assignedTo,
                                     @RequestParam String notes,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate scheduledFor,
                                     @RequestParam String priority,
                                     RedirectAttributes redirectAttributes) {

        // --- ADDED: Backend Date Validation ---
        if (scheduledFor.isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("error", "The scheduled date cannot be in the past.");
            return "redirect:/housekeeping";
        }

        if (roomId == null || assignedTo.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Room and Assigned To fields are required.");
            return "redirect:/housekeeping";
        }

        housekeepingService.createCleaningTask(roomId, assignedTo, notes, scheduledFor, priority);
        redirectAttributes.addFlashAttribute("success", "Cleaning task created successfully!");
        return "redirect:/housekeeping";
    }

    @PostMapping("/tasks/{id}/update-status")
    public String updateTaskStatus(@PathVariable("id") Long taskId, @RequestParam String status) {
        housekeepingService.updateTaskStatus(taskId, status);
        return "redirect:/housekeeping";
    }

    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable("id") Long taskId, RedirectAttributes redirectAttributes) {
        try {
            housekeepingService.deleteTask(taskId);
            redirectAttributes.addFlashAttribute("success", "Task deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting task.");
        }
        return "redirect:/housekeeping";
    }
}