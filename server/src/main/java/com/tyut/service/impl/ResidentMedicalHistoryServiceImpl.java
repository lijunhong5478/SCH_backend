package com.tyut.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tyut.annotation.DataBackUp;
import com.tyut.constant.AccountConstant;
import com.tyut.constant.ModuleConstant;
import com.tyut.context.BaseContext;
import com.tyut.entity.ResidentMedicalHistory;
import com.tyut.exception.BaseException;
import com.tyut.mapper.ResidentMedicalHistoryMapper;
import com.tyut.result.PageResult;
import com.tyut.service.ResidentMedicalHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class ResidentMedicalHistoryServiceImpl implements ResidentMedicalHistoryService {

    @Autowired
    private ResidentMedicalHistoryMapper residentMedicalHistoryMapper;
    @DataBackUp(module = ModuleConstant.MEDICAL_HISTORY_INSERT)
    @Override
    public void saveByDoctor(ResidentMedicalHistory medicalHistory) {

        // 检查是否已存在该健康档案的病史记录
        LambdaQueryWrapper<ResidentMedicalHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ResidentMedicalHistory::getRecordId, medicalHistory.getRecordId())
                .eq(ResidentMedicalHistory::getIsDeleted, AccountConstant.NOT_DELETE);
        // 设置默认值
        medicalHistory.setIsDeleted(AccountConstant.NOT_DELETE);
        medicalHistory.setCreateTime(LocalDateTime.now());
        medicalHistory.setUpdateTime(LocalDateTime.now());

        residentMedicalHistoryMapper.insert(medicalHistory);
        log.info("医生新增病史记录成功，记录ID: {}", medicalHistory.getId());
    }

    @Override
    public ResidentMedicalHistory getById(Long id) {
        ResidentMedicalHistory record = residentMedicalHistoryMapper.selectById(id);
        if (record == null || record.getIsDeleted() == AccountConstant.IS_DELETE) {
            throw new BaseException("病史记录不存在或已被删除");
        }

        // 权限检查
        Integer currentUserRole = BaseContext.getCurrentRole();
        Long currentUserId = BaseContext.getCurrentId();

        if (currentUserRole == null) {
            throw new BaseException("用户未登录");
        }

        // 医生可以查看所有记录，居民只能查看自己的记录
        if (currentUserRole == AccountConstant.ROLE_RESIDENT) {
            // 需要验证该记录是否属于当前居民（通过健康档案关联）
            log.info("居民查询病史记录，ID: {}", id);
        } else if (currentUserRole == AccountConstant.ROLE_DOCTOR) {
            log.info("医生查询病史记录，ID: {}", id);
        }

        return record;
    }

    @Override
    public ResidentMedicalHistory getByResident(Long recordId) {

        LambdaQueryWrapper<ResidentMedicalHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ResidentMedicalHistory::getRecordId, recordId)
                .eq(ResidentMedicalHistory::getIsDeleted, AccountConstant.NOT_DELETE);

        ResidentMedicalHistory medicalHistory = residentMedicalHistoryMapper.selectOne(queryWrapper);

        if (medicalHistory == null) {
            throw new BaseException("暂无病史记录");
        }

        log.info("居民查询病史记录，健康档案ID: {}", recordId);
        return medicalHistory;
    }

    @Override
    public PageResult listByDoctor(Integer pageNum, Integer pageSize, Long recordId) {

        Page<ResidentMedicalHistory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ResidentMedicalHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ResidentMedicalHistory::getRecordId, recordId)
                .eq(ResidentMedicalHistory::getIsDeleted, AccountConstant.NOT_DELETE)
                .orderByDesc(ResidentMedicalHistory::getCreateTime);

        IPage<ResidentMedicalHistory> pageData = residentMedicalHistoryMapper.selectPage(page, queryWrapper);

        log.info("医生查询病史记录列表，健康档案ID: {}, 记录数: {}", recordId, pageData.getTotal());
        return new PageResult(pageData.getTotal(), pageData.getRecords());
    }
}
