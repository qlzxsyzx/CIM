package com.qlzxsyzx.web.controller;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.resource.annotation.AuthenticationDetails;
import com.qlzxsyzx.web.dto.CreateGroupDto;
import com.qlzxsyzx.web.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/group")
public class GroupController {
    @Autowired
    private GroupService groupService;

    @GetMapping("/getGroupList")
    public ResponseEntity getGroupList(@AuthenticationDetails("userId") Long userId) {
        return groupService.getGroupList(userId);
    }

    @PostMapping("/createGroup")
    public ResponseEntity createGroup(@AuthenticationDetails("userId") Long userId, @RequestBody CreateGroupDto createGroupDto) {
        return groupService.createGroup(userId, createGroupDto);
    }
}
