package com.tyut.service;

import java.util.List;
import java.util.Map;

public interface GraphService {
    List<Map<String,Object>> getResidentAgeCount();
    List<Map<String,Object>> getDoctorDeptCount();
    List<Map<String,Object>> getAppointmentCount();
}
