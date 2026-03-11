package com.tyut.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tyut.annotation.DataBackUp;
import com.tyut.constant.AccountConstant;
import com.tyut.constant.ModuleConstant;
import com.tyut.dto.DiagnosisReportQueryDTO;
import com.tyut.entity.DiagnosisReport;
import com.tyut.mapper.DiagnosisReportMapper;
import com.tyut.mapper.MedicalVisitMapper;
import com.tyut.result.PageResult;
import com.tyut.service.DiagnosisReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DiagnosisReportServiceImpl implements DiagnosisReportService {
    @Autowired
    private DiagnosisReportMapper diagnosisReportMapper;
    @Autowired
    private MedicalVisitMapper medicalVisitMapper;
    @DataBackUp(module = ModuleConstant.DIAGNOSIS_RECORD_INSERT)
    @Override
    public void save(DiagnosisReport diagnosisReport) {
        diagnosisReport.setCreateTime(LocalDateTime.now());
        diagnosisReport.setIsDeleted(AccountConstant.NOT_DELETE);
        diagnosisReportMapper.insert(diagnosisReport);
    }

    @Override
    public DiagnosisReport getById(Long id) {
        return diagnosisReportMapper.selectById(id);
    }

    @Override
    public PageResult list(DiagnosisReportQueryDTO queryDTO) {
        LambdaQueryWrapper<DiagnosisReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DiagnosisReport::getHealthRecordId, queryDTO.getHealthRecordId());
        if (queryDTO.getCreateDate() != null) {
            queryWrapper.between(DiagnosisReport::getCreateTime,
                    queryDTO.getCreateDate().atTime(0, 0),
                    queryDTO.getCreateDate().atTime(23, 59));
        }
        Page<DiagnosisReport> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<DiagnosisReport> pageData = diagnosisReportMapper.selectPage(page, queryWrapper);
        return new PageResult(pageData.getTotal(), pageData.getRecords());
    }
    @DataBackUp(module = ModuleConstant.DIAGNOSIS_RECORD_UPDATE)
    @Override
    public void update(DiagnosisReport diagnosisReport) {
        diagnosisReportMapper.updateById(diagnosisReport);
    }

    @Override
    public Boolean check(Long diagnosisId,Long doctorId) {
        DiagnosisReport diagnosisReport = diagnosisReportMapper.selectById(diagnosisId);
        if (diagnosisReport != null&&diagnosisReport.getIsDeleted()==AccountConstant.NOT_DELETE) {
            Long visitId = diagnosisReport.getVisitId();
            return doctorId.equals(medicalVisitMapper.selectById(visitId).getDoctorId());
        }
        return false;
    }

    @Override
    public DiagnosisReport getByVisitId(Long visitId) {
        LambdaQueryWrapper<DiagnosisReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DiagnosisReport::getVisitId, visitId);
        return diagnosisReportMapper.selectOne(queryWrapper);
    }
}
