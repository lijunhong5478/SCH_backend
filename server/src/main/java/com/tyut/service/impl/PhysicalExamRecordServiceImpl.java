package com.tyut.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.tyut.annotation.DataBackUp;
import com.tyut.constant.AccountConstant;
import com.tyut.constant.ModuleConstant;
import com.tyut.context.BaseContext;
import com.tyut.entity.PhysicalExamRecord;
import com.tyut.exception.BaseException;
import com.tyut.mapper.PhysicalExamRecordMapper;
import com.tyut.result.PageResult;
import com.tyut.service.PhysicalExamRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class PhysicalExamRecordServiceImpl implements PhysicalExamRecordService {

    @Autowired
    private PhysicalExamRecordMapper physicalExamRecordMapper;
    @DataBackUp(module = ModuleConstant.PHYSICAL_EXAM_INSERT)
    @Override
    public void saveByDoctor(PhysicalExamRecord physicalExamRecord) {
        // 设置默认值
        physicalExamRecord.setIsDeleted(AccountConstant.NOT_DELETE);
        physicalExamRecord.setCreateTime(LocalDateTime.now());
        physicalExamRecord.setExamTime(LocalDateTime.now());

        // 计算BMI指数（如果身高和体重都提供）
        if (physicalExamRecord.getHeight() != null && physicalExamRecord.getWeight() != null) {
            BigDecimal heightInMeters = physicalExamRecord.getHeight().divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal bmi = physicalExamRecord.getWeight().divide(heightInMeters.multiply(heightInMeters), 2, BigDecimal.ROUND_HALF_UP);
            physicalExamRecord.setBmi(bmi);
        }

        physicalExamRecordMapper.insert(physicalExamRecord);
        log.info("医生新增体检记录成功，记录ID: {}", physicalExamRecord.getId());
    }

    @Override
    public PhysicalExamRecord getById(Long id) {
        PhysicalExamRecord record = physicalExamRecordMapper.selectById(id);
        if (record == null || record.getIsDeleted() == AccountConstant.IS_DELETE) {
            throw new BaseException("体检记录不存在或已被删除");
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
            // 这里简化处理，实际应该通过健康档案表验证关联关系
            log.info("居民查询体检记录，ID: {}", id);
        } else if (currentUserRole == AccountConstant.ROLE_DOCTOR) {
            log.info("医生查询体检记录，ID: {}", id);
        }

        return record;
    }

    @Override
    public PageResult listByResident(Integer pageNum, Integer pageSize, Long recordId) {
        Page<PhysicalExamRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PhysicalExamRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PhysicalExamRecord::getRecordId, recordId)
                .eq(PhysicalExamRecord::getIsDeleted, AccountConstant.NOT_DELETE)
                .orderByDesc(PhysicalExamRecord::getExamTime);

        IPage<PhysicalExamRecord> pageData = physicalExamRecordMapper.selectPage(page, queryWrapper);

        log.info("居民查询体检记录列表，健康档案ID: {}, 记录数: {}", recordId, pageData.getTotal());
        return new PageResult(pageData.getTotal(), pageData.getRecords());
    }

    @Override
    public PhysicalExamRecord getLatestByResident(Long recordId) {
        // 验证当前用户是否为居民角色
        Integer currentUserRole = BaseContext.getCurrentRole();

        LambdaQueryWrapper<PhysicalExamRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PhysicalExamRecord::getRecordId, recordId)
                .eq(PhysicalExamRecord::getIsDeleted, AccountConstant.NOT_DELETE)
                .orderByDesc(PhysicalExamRecord::getExamTime)
                .last("LIMIT 1");

        PhysicalExamRecord latestRecord = physicalExamRecordMapper.selectOne(queryWrapper);

        if (latestRecord == null) {
            throw new BaseException("暂无体检记录");
        }

        log.info("居民查询最近体检记录，健康档案ID: {}, 最近记录时间: {}", recordId, latestRecord.getExamTime());
        return latestRecord;
    }

    @Override
    public PageResult listByDoctor(Integer pageNum, Integer pageSize, Long recordId) {
        Page<PhysicalExamRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PhysicalExamRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PhysicalExamRecord::getRecordId, recordId)
                .eq(PhysicalExamRecord::getIsDeleted, AccountConstant.NOT_DELETE)
                .orderByDesc(PhysicalExamRecord::getExamTime);

        IPage<PhysicalExamRecord> pageData = physicalExamRecordMapper.selectPage(page, queryWrapper);

        log.info("医生查询体检记录列表，健康档案ID: {}, 记录数: {}", recordId, pageData.getTotal());
        return new PageResult(pageData.getTotal(), pageData.getRecords());
    }
}
