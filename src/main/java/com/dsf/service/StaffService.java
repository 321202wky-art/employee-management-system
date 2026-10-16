package com.dsf.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dsf.entity.Staff;

import java.util.List;
import java.util.Map;

public interface StaffService {
    void saveStaff(Staff staff);
    IPage<Staff> pageQuery(String name, String gender, String deptName, String education, String nativePlace,
                           String workStatus, int page, int size);
    void updateStaff(Staff staff);
    void deleteStaffById(List<Integer> id);
    void deleteStaffBatch(List<Integer> ids);
    Map<String, Object> getStatistics();
    List<Staff> getAllStaff();
}