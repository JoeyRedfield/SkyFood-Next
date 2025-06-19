package com.sky.dto;

//import io.swagger.annotations.ApiModel;
//import io.swagger.annotations.ApiModelProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.io.Serializable;

@Data
//@ApiModel(description = "员工登录时传递的数据模型")
@Schema(description = "员工登录时传递的数据模型")
public class EmployeeLoginDTO implements Serializable {

    //    @ApiModelProperty("用户名")
    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户名")
    private String password;

}
