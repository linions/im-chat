package com.lld.im.service.role.dao;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "im_role_menu")
public class ImRoleMenuEntity {

    @TableId
    private int roleId;

    private int menuId;
}
