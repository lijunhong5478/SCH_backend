package com.tyut.service;

import com.tyut.dto.*;
import com.tyut.result.PageResult;
import com.tyut.vo.AdminDetailVO;
import com.tyut.vo.DoctorDetailVO;
import com.tyut.vo.LoginUserVO;
import com.tyut.vo.ResidentDetailVO;

public interface UserService {
    LoginUserVO login(LoginDTO loginDTO);

    AdminDetailVO getAdminById(Long id);

    DoctorDetailVO getDoctorById(Long id);

    ResidentDetailVO getResidentById(Long id);

    void registerResident(ResidentRegisterDTO residentRegisterDTO);

    void updateAdmin(UpdateProfileDTO updateProfileDTO);

    void updateDoctor(UpdateProfileDTO updateProfileDTO);

    void updateResident(UpdateProfileDTO updateProfileDTO);

    void updatePassword(String oldPassword, String newPassword);

    void resetPassword(Long userId);

    void updateStatus(Long id, Integer status);

    void deleteUser(Long id);

    void revertUser(Long id);

    void logout();

    PageResult list(AccountQueryDTO accountQueryDTO);

    String getRealNameById(Long id);
}
