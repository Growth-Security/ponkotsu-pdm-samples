package info.syachiku.formkeep;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmployeeForm {
    @NotBlank(message = "氏名を入力してください")
    private String name;

    @NotBlank(message = "部署を選択してください")
    private String departmentCode;

    @Email(message = "メールアドレスの形式を確認してください")
    private String email;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
