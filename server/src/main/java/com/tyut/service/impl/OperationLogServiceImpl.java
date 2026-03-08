package com.tyut.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tyut.dto.OperationLogQueryDTO;
import com.tyut.entity.OperationLog;
import com.tyut.mapper.OperationLogMapper;
import com.tyut.mapper.UserMapper;
import com.tyut.result.PageResult;
import com.tyut.service.OperationLogService;
import com.tyut.vo.OperationLogVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperationLogServiceImpl implements OperationLogService {
    @Autowired
    private OperationLogMapper operationLogMapper;
    @Autowired
    private UserMapper userMapper;
    @Override
    public PageResult list(OperationLogQueryDTO queryDTO) {
        LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(OperationLog::getCreateTime, queryDTO.getEndDate())
                .ge(OperationLog::getCreateTime, queryDTO.getStartDate());
        Page<OperationLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<OperationLog> pageData = operationLogMapper.selectPage(page, queryWrapper);
        List<OperationLog> records = pageData.getRecords();
        List<OperationLogVO> voList = records.stream().map(log -> {
            OperationLogVO logVO = new OperationLogVO();
            BeanUtils.copyProperties(log, logVO);
            logVO.setUserName(userMapper.selectById(log.getUserId()).getUsername());
            return logVO;
        }).collect(Collectors.toList());
        return new PageResult(pageData.getTotal(), voList);
    }

    @Override
    public void deleteById(Long id) {
        operationLogMapper.deleteById(id);
    }
}
