package nus.iss.se.magicbag.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import nus.iss.se.magicbag.dto.TaskQo;
import nus.iss.se.magicbag.dto.event.MerchantRegisterEvent;
import nus.iss.se.magicbag.entity.AdminTask;


public interface IAdminTaskService extends IService<AdminTask> {
    IPage<AdminTask> getTasks(TaskQo qo);

    void createTask(MerchantRegisterEvent event);
    void claimTask(Long taskId);
    void approveTask(Long taskId);
    void rejectTask(Long taskId,String comment);

}
