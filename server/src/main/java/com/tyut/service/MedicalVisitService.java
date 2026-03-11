package com.tyut.service;

import com.tyut.dto.MedicalVisitQueryDTO;
import com.tyut.entity.MedicalVisit;
import com.tyut.result.PageResult;
import com.tyut.vo.MedicalVisitVO;

public interface MedicalVisitService {
    MedicalVisitVO getById(Long id);
    void save(MedicalVisit medicalVisit);
    void update(MedicalVisit medicalVisit);
    PageResult list(MedicalVisitQueryDTO medicalVisitQueryDTO);
    Boolean contains(Long medicalVisitId, Long doctorId);
    MedicalVisitVO getByAppointId(Long appointId);
}
