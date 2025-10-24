package nus.iss.se.magicbag.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.common.Result;
import nus.iss.se.magicbag.dto.TaskQo;
import nus.iss.se.magicbag.entity.AdminTask;
import nus.iss.se.magicbag.service.IAdminTaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final IAdminTaskService adminTaskService;

    @GetMapping("/task")
    public Result<?> getTasks(TaskQo qo){
        IPage<AdminTask> tasks = adminTaskService.getTasks(qo);
        return Result.success(tasks);
    }

    @PostMapping("/task/{taskId}/claim")
    public Result<?> claimTask(@PathVariable Long taskId) {
        adminTaskService.claimTask(taskId);
        return Result.success();
    }

    @PostMapping("/task/{taskId}/approve")
    public Result<?> approveTask(@PathVariable Long taskId) {
        adminTaskService.approveTask(taskId);
        return Result.success();
    }

    @PostMapping("/task/{taskId}/reject")
    public Result<?> rejectTask(@PathVariable Long taskId, @RequestParam String comment) {
        adminTaskService.rejectTask(taskId,comment);
        return Result.success();
    }
}
