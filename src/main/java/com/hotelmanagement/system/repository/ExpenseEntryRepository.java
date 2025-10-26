package com.hotelmanagement.system.repository;

import com.hotelmanagement.system.model.ExpenseEntry;
import com.hotelmanagement.system.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseEntryRepository extends JpaRepository<ExpenseEntry, Long> {
    List<ExpenseEntry> findByReport(Report report);
    List<ExpenseEntry> findByReportOrderByEntryDateDesc(Report report);
    List<ExpenseEntry> findByEntryDateBetween(LocalDate start, LocalDate end);
    List<ExpenseEntry> findByCategory(String category);
    List<ExpenseEntry> findByReportIdOrderByEntryDateDesc(Long reportId);
}