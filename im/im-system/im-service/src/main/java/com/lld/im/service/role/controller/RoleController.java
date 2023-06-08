package com.lld.im.service.role.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.model.SyncReq;
import com.lld.im.service.role.model.req.*;
import com.lld.im.service.role.service.ImRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @className: RoleController
 * @author: linion
 * @date: 2023/4/30 16:41
 * @version: 1.0
 */

@RestController
@CrossOrigin
@RequestMapping("v1/role")
public class RoleController {

    @Autowired
    private ImRoleService imRoleService;

    @PostMapping("/getByPage")
    public ResponseVO roleGetByPage(@RequestBody @Validated GetRoleReq req)  {
        return imRoleService.getByPage(req);
    }

    @PostMapping("/create")
    public ResponseVO createRole(HttpServletRequest request, @RequestBody @Validated RoleReq req)  {
        return imRoleService.createRole(request,req);
    }

    @PostMapping("/deleteById")
    public ResponseVO deleteRoleById(HttpServletRequest request,@RequestBody @Validated DeleteRoleReq req)  {
        return imRoleService.deleteRoleById(request,req);
    }

    @PostMapping("/update")
    public ResponseVO updateRoleById(HttpServletRequest request,@RequestBody @Validated UpdateRoleReq req)  {
        return imRoleService.updateRoleById(request,req);
    }

    @PutMapping("/changeStatus/{roleId}/{status}/{operator}")
    public ResponseVO updateRoleStatus(HttpServletRequest request,@PathVariable int roleId,@PathVariable int status,@PathVariable String operator)  {
        return imRoleService.updateRoleStatus(request,roleId,status,operator);
    }

    @PostMapping("/bindRole")
    public ResponseVO bindRole(HttpServletRequest request,@RequestBody BindRoleReq req)  {
        return imRoleService.bindRole(request,req);
    }

}
