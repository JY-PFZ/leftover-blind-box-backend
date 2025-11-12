package nus.iss.se.magicbag.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.dto.MerchantDto;
import nus.iss.se.magicbag.dto.MerchantRegisterDto;
import nus.iss.se.magicbag.dto.MerchantUpdateDto;
import nus.iss.se.magicbag.dto.event.MerchantProcessedEvent;
import nus.iss.se.magicbag.entity.Merchant;

import java.util.List;

public interface IMerchantService extends IService<Merchant> {


    /**
     * 🟢 新增：获取所有状态为 'approved' 的商家列表
     */
    List<MerchantDto> getAllApprovedMerchants();

    /**
     * 根据ID获取商家详情
     */
    MerchantDto getMerchantById(Integer id);

    /**
     * 🟢 新增：根据用户ID查找商家信息
     * @param userId 用户ID (关联 users 表的 id)
     * @return 商家 DTO，如果不存在则返回 null
     */
    MerchantDto findByUserId(Integer userId);

    /**
     * 更新商家信息
     * @param merchantDto 商家更新信息
     * @param currentUser 当前登录用户上下文
     */
    void updateMerchantProfile(MerchantUpdateDto merchantDto, UserContext currentUser);

    /**
     * 分页获取按评分排序的商家列表
     * @param current 当前页码
     * @param size 每页数量
     * @param minScore 最低评分 (可选)
     * @return 分页结果
     */
    IPage<MerchantDto> sortedMerchantsByScore(Integer current, Integer size,  Integer minScore);

    void registerMerchant(MerchantRegisterDto dto);

    void handleRegisterResult(MerchantProcessedEvent event);
}
