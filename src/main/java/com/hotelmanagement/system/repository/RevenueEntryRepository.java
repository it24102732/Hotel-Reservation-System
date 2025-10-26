package com.hotelmanagement.system.repository;

import com.hotelmanagement.system.model.RevenueEntry;
import com.hotelmanagement.system.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RevenueEntryRepository extends JpaRepository<RevenueEntry, Long> {
    List<RevenueEntry> findByReport(Report report);
    List<RevenueEntry> findByReportOrderByEntryDateDesc(Report report);
    List<RevenueEntry> findByEntryDateBetween(LocalDate start, LocalDate end);
    List<RevenueEntry> findBySource(String source);
    List<RevenueEntry> findByReportIdOrderByEntryDateDesc(Long reportId);
}