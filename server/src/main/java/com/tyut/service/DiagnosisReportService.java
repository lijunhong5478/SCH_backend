package com.tyut.service;

import com.tyut.dto.DiagnosisReportQueryDTO;
import com.tyut.entity.DiagnosisReport;
import com.tyut.result.PageResult;

public interface DiagnosisReportService {
    void save(DiagnosisReport diagnosisReport);
    DiagnosisReport getById(Long id);
    PageResult list(DiagnosisReportQueryDTO queryDTO);
    void update(DiagnosisReport diagnosisReport);
    Boolean check(Long diagnosisId,Long doctorId);
    DiagnosisReport getByVisitId(Long visitId);
}
