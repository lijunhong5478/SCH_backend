package com.tyut.service;

import com.tyut.dto.AddDoctorDTO;
import com.tyut.dto.DoctorQueryDTO;
import com.tyut.dto.DoctorScheduleUpdateDTO;
import com.tyut.entity.DoctorSchedule;
import com.tyut.result.PageResult;

import java.util.List;

public interface DoctorService {
    void registerDoctor(AddDoctorDTO addDoctorDTO);
    void doctorSchedule(DoctorScheduleUpdateDTO dto);
    PageResult list(DoctorQueryDTO queryDTO);
    List<DoctorSchedule> getDoctorScheduleById(Long id);
}
