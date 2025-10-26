package com.hotelmanagement.system.repository;

import com.hotelmanagement.system.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReportType(String reportType);
    List<Report> findByGeneratedBy(String generatedBy);
    List<Report> findByStatus(String status);
    List<Report> findByGeneratedDateBetween(LocalDateTime start, LocalDateTime end);
    List<Report> findByReportTypeAndStatus(String reportType, String status);
    List<Report> findByReportTypeOrderByGeneratedDateDesc(String reportType);
    List<Report> findAllByOrderByGeneratedDateDesc();
}