package com.hotelmanagement.system.repository;

import com.hotelmanagement.system.model.CleaningTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CleaningTaskRepository extends JpaRepository<CleaningTask, Long> {
    List<CleaningTask> findByStatus(String status);

    // New method for summary card
    long countByStatus(String status);

    // New method for summary card
    @Query("SELECT COUNT(t) FROM CleaningTask t WHERE t.status = 'COMPLETED' AND t.completedAt >= CURRENT_DATE")
    long countCompletedToday();
}