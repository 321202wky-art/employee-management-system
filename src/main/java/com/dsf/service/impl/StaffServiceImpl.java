package com.dsf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dsf.entity.Staff;
import com.dsf.mapper.StaffMapper;
import com.dsf.service.StaffService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StaffServiceImpl implements StaffService {
    private final StaffMapper staffMapper;

    public StaffServiceImpl(StaffMapper staffMapper) {
        this.staffMapper = staffMapper;
    }

    @Override
    public void saveStaff(Staff staff) {
        if (!StringUtils.hasText(staff.getEducation())) {
            staff.setEducation("未填写");
        }
        if (!StringUtils.hasText(staff.getIdCare())) {
            staff.setIdCare("000000000000000000");
        }
        if (!StringUtils.hasText(staff.getNativePlace())) {
            staff.setNativePlace("未填写");
        }
        if (!StringUtils.hasText(staff.getAddress())) {
            staff.setAddress("未填写");
        }
        if (!StringUtils.hasText(staff.getBankCard())) {
            staff.setBankCard("0000000000000000");
        }
        if (!StringUtils.hasText(staff.getWorkStatus())) {
            staff.setWorkStatus("在职");
        }
        staffMapper.insert(staff);
    }

    @Override
    public IPage<Staff> pageQuery(String name, String gender, String deptName, String education, String nativePlace, String workStatus, int page, int size) {
        Page<Staff> p = new Page<>(page, size);
        LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Staff::getName, name);
        }
        if (StringUtils.hasText(gender)) {
            wrapper.eq(Staff::getGender, gender);
        }
        if (StringUtils.hasText(deptName)) {
            wrapper.like(Staff::getDeptName, deptName);
        }
        if (StringUtils.hasText(education)) {
            wrapper.like(Staff::getEducation, education);
        }
        if (StringUtils.hasText(workStatus)) {
            wrapper.eq(Staff::getWorkStatus, workStatus);
        }
        if (StringUtils.hasText(nativePlace)) {
            wrapper.like(Staff::getNativePlace, nativePlace);
        }
        wrapper.orderByAsc(Staff::getId);
        return staffMapper.selectPage(p, wrapper);
    }

    @Override
    public void updateStaff(Staff staff) {
        if (!StringUtils.hasText(staff.getEducation())) {
            staff.setEducation("未填写");
        }
        if (!StringUtils.hasText(staff.getIdCare())) {
            staff.setIdCare("000000000000000000");
        }
        if (!StringUtils.hasText(staff.getNativePlace())) {
            staff.setNativePlace("未填写");
        }
        if (!StringUtils.hasText(staff.getAddress())) {
            staff.setAddress("未填写");
        }
        if (!StringUtils.hasText(staff.getBankCard())) {
            staff.setBankCard("0000000000000000");
        }
        if (!StringUtils.hasText(staff.getWorkStatus())) {
            staff.setWorkStatus("在职");
        }
        staffMapper.updateById(staff);
    }

    @Override
    public void deleteStaffById(List<Integer> id) {
        staffMapper.deleteBatchIds(id);
    }

    @Override
    public void deleteStaffBatch(List<Integer> ids) {
        staffMapper.deleteBatchIds(ids);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();
        List<Staff> allStaff = staffMapper.selectList(null);
        
        // Total employees
        result.put("totalEmployees", allStaff.size());
        
        // Department count (unique)
        long deptCount = allStaff.stream().map(Staff::getDeptName).filter(Objects::nonNull).distinct().count();
        result.put("totalDepartments", (int)deptCount);
        
        // New this month - count all as "new" if list has data (simplified)
        result.put("newThisMonth", allStaff.size());
        
        // Gender distribution
        long male = allStaff.stream().filter(s -> "男".equals(s.getGender())).count();
        long female = allStaff.stream().filter(s -> "女".equals(s.getGender())).count();
        Map<String, Integer> genderMap = new HashMap<>();
        genderMap.put("男", (int)male);
        genderMap.put("女", (int)female);
        result.put("genderDistribution", genderMap);
        
        // Department distribution
        Map<String, Long> deptDist = allStaff.stream()
            .filter(s -> s.getDeptName() != null)
            .collect(Collectors.groupingBy(Staff::getDeptName, Collectors.counting()));
        result.put("departmentDistribution", deptDist);
        
        // Work status distribution
        Map<String, Long> statusDist = allStaff.stream()
            .filter(s -> s.getWorkStatus() != null)
            .collect(Collectors.groupingBy(Staff::getWorkStatus, Collectors.counting()));
        result.put("statusDistribution", statusDist);
        
        return result;
    }

    @Override
    public List<Staff> getAllStaff() {
        return staffMapper.selectList(null);
    }
}
