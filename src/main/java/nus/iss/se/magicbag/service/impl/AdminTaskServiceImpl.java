package nus.iss.se.magicbag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.auth.common.UserContextHolder;
import nus.iss.se.magicbag.common.constant.ResultStatus;
import nus.iss.se.magicbag.common.constant.TaskStatus;
import nus.iss.se.magicbag.common.constant.TaskType;
import nus.iss.se.magicbag.common.exception.BusinessException;
import nus.iss.se.magicbag.dto.TaskQo;
import nus.iss.se.magicbag.dto.event.MerchantProcessedEvent;
import nus.iss.se.magicbag.dto.event.MerchantRegisterEvent;
import nus.iss.se.magicbag.entity.AdminTask;
import nus.iss.se.magicbag.mapper.AdminTaskMapper;
import nus.iss.se.magicbag.service.IAdminTaskService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminTaskServiceImpl extends ServiceImpl<AdminTaskMapper, AdminTask> implements IAdminTaskService {
    private final ObjectMapper objectMapper;
    private final UserContextHolder userContextHolder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public IPage<AdminTask> getTasks(TaskQo qo) {
        // 设置默认分页参数（防止空指针）
        int pageNum = (qo.getPageNum() == null || qo.getPageNum() <= 0) ? 1 : qo.getPageNum();
        int pageSize = (qo.getPageSize() == null || qo.getPageSize() <= 0) ? 10 : qo.getPageSize();

        // 构建分页对象
        Page<AdminTask> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<AdminTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(qo.getId() != null,AdminTask::getId, qo.getId())
                .eq(qo.getType() != null, AdminTask::getType, qo.getType())
                .eq(qo.getStatus() != null, AdminTask::getStatus, qo.getStatus())
                .eq(qo.getApplicant() != null, AdminTask::getApplicant, qo.getApplicant())
                .eq(qo.getStatus() != null && !qo.getStatus().equals(TaskStatus.PENDING.getCode()),AdminTask::getOperator,userContextHolder.getCurrentUser().getId())
                .orderByDesc(AdminTask::getStartTime);

        // 执行分页查询
        return this.page(page, wrapper);
    }

    @Override
    @EventListener
    public void createTask(MerchantRegisterEvent event) {
        AdminTask task = new AdminTask();

        try {
            String data = objectMapper.writeValueAsString(event);
            task.setData(data);
            task.setApplicant(event.userId());
            task.setTitle("Merchant registration review - " + event.shopName());

            task.setType(TaskType.MERCHANT_APPROVAL.getCode());
            task.setStatus(TaskStatus.PENDING.getCode());
            task.setStartTime(new Date());

            save(task);
        } catch (JsonProcessingException e) {
            log.info("数据序列化失败：{}", ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    @Transactional
    public void claimTask(Long taskId) {
        // 1. 查询当前任务
        AdminTask task = this.getById(taskId);
        if (task == null) {
            throw new BusinessException(ResultStatus.FAIL, "Task Not Found");
        }

        // 2. 校验状态：只能从“待处理”领取
        if (!TaskStatus.PENDING.getCode().equals(task.getStatus())) {
            throw new BusinessException(ResultStatus.FAIL, "Task Not be Claim");
        }

        // 3. 更新状态为“处理中”，并记录处理人（假设从上下文获取）
        LambdaUpdateWrapper<AdminTask> wrapper = new LambdaUpdateWrapper<AdminTask>()
                .set(AdminTask::getStatus, TaskStatus.PROCESSING.getCode())
                .set(AdminTask::getOperator, userContextHolder.getCurrentUser().getId())
                .eq(AdminTask::getId, taskId)
                .eq(AdminTask::getStatus, TaskStatus.PENDING.getCode());

        this.update(wrapper);
    }

    @Override
    @Transactional
    public void approveTask(Long taskId) {
        AdminTask task = this.getById(taskId);
        if (task == null) {
            throw new BusinessException(ResultStatus.FAIL, "Task Not Found");
        }
        if (!TaskStatus.PROCESSING.getCode().equals(task.getStatus())) {
            throw new BusinessException(ResultStatus.FAIL, "This operation cannot be performed in the current state");
        }

        Date now = new Date();
        LambdaUpdateWrapper<AdminTask> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(AdminTask::getStatus, TaskStatus.APPROVED.getCode())
                .set(AdminTask::getEndTime, now)
                .eq(AdminTask::getId, taskId)
                .eq(AdminTask::getStatus, TaskStatus.PROCESSING.getCode());
        update(wrapper);

        MerchantProcessedEvent event = new MerchantProcessedEvent(task.getApplicant(),TaskStatus.APPROVED.getCode(),task.getOperator(),now,null);
        eventPublisher.publishEvent(event);
    }

    @Override
    @Transactional
    public void rejectTask(Long taskId,String comment) {
        AdminTask task = this.getById(taskId);
        if (task == null) {
            throw new BusinessException(ResultStatus.FAIL, "Task Not Found");
        }
        if (!TaskStatus.PROCESSING.getCode().equals(task.getStatus())) {
            throw new BusinessException(ResultStatus.FAIL, "This operation cannot be performed in the current state");
        }

        Date now = new Date();
        LambdaUpdateWrapper<AdminTask> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(AdminTask::getStatus, TaskStatus.REJECTED.getCode())
                .set(AdminTask::getEndTime, now)
                .set(AdminTask::getComment,comment)
                .eq(AdminTask::getId, taskId)
                .eq(AdminTask::getStatus, TaskStatus.PROCESSING.getCode());
        update(wrapper);

        MerchantProcessedEvent event = new MerchantProcessedEvent(task.getApplicant(),TaskStatus.APPROVED.getCode(),task.getOperator(),now,comment);
        eventPublisher.publishEvent(event);
    }
}
