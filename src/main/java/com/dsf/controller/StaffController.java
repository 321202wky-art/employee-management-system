package com.dsf.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dsf.entity.Staff;
import com.dsf.service.StaffService;
import com.dsf.util.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
public class StaffController {
    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @PostMapping
    public Result<Void> addStaff(@RequestBody Staff staff) {
        staffService.saveStaff(staff);
        return Result.success();
    }

    @PostMapping("/page")
    public Result<IPage<Staff>> pageQuery(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String deptName,
            @RequestParam(required = false) String education,
            @RequestParam(required = false) String workStatus,
            @RequestParam(required = false) String nativePlace,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(staffService.pageQuery(name, gender, deptName, education, nativePlace, workStatus, page, size));
    }

    @PutMapping
    public Result<Void> update(@RequestBody Staff staff) {
        staffService.updateStaff(staff);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        staffService.deleteStaffById(List.of(id));
        return Result.success();
    }

    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody List<Integer> ids) {
        staffService.deleteStaffById(ids);
        return Result.success();
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        return Result.success(staffService.getStatistics());
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        try {
            List<Staff> allStaff = staffService.getAllStaff();
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("员工信息表.csv", StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            
            // Write BOM for Excel UTF-8 support
            response.getOutputStream().write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8));
            
            // Header row
            writer.println("工号,姓名,性别,年龄,岗位,手机号,学历,在职状态,籍贯,基本工资,身份证号,居住地址,银行卡号");
            
            // Data rows
            for (Staff s : allStaff) {
                writer.print(nullToEmpty(s.getId()) + ",");
                writer.print(escapeCsv(s.getName()) + ",");
                writer.print(escapeCsv(s.getGender()) + ",");
                writer.print(nullToEmpty(s.getAge()) + ",");
                writer.print(escapeCsv(s.getDeptName()) + ",");
                writer.print(escapeCsv(s.getPhone()) + ",");
                writer.print(escapeCsv(s.getEducation()) + ",");
                writer.print(escapeCsv(s.getWorkStatus()) + ",");
                writer.print(escapeCsv(s.getNativePlace()) + ",");
                writer.print(nullToEmpty(s.getBasicSalary()) + ",");
                writer.print(escapeCsv(s.getIdCare()) + ",");
                writer.print(escapeCsv(s.getAddress()) + ",");
                writer.print(escapeCsv(s.getBankCard()));
                writer.println();
            }
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
    
    private String nullToEmpty(Object val) {
        return val == null ? "" : val.toString();
    }
}