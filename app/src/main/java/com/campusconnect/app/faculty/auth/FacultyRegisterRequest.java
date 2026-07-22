package com.campusconnect.app.faculty.auth;

public class FacultyRegisterRequest {
    private String full_name;
    private String email;
    private String employee_id;
    private String department;
    private String designation;
    private String password;

    public FacultyRegisterRequest(String fullName, String email, String employeeId,
                                  String department, String designation, String password) {
        this.full_name = fullName;
        this.email = email;
        this.employee_id = employeeId;
        this.department = department;
        this.designation = designation;
        this.password = password;
    }
}
