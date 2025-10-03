package nus.iss.se.magicbag.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.dto.MerchantDto;
import nus.iss.se.magicbag.dto.MerchantUpdateDto;

import java.util.List;

public interface IMerchantService {
    
    /**
     * 获取所有已审核的商家列表
     */
    List<MerchantDto> getAllMerchants();
    
    /**
     * 根据ID获取商家详情
     */
    MerchantDto getMerchantById(Integer id);
    
    /**
     * 根据用户ID获取对应的商家ID
     * @param userId 用户ID
     * @return 商家ID，如果不存在则返回null
     */
    Integer getMerchantIdByUserId(Integer userId);
    
    /**
     * 更新商家信息
     * @param merchantDto 商家更新信息
     * @param currentUser 当前登录用户上下文
     */
    void updateMerchantProfile(MerchantUpdateDto merchantDto, UserContext currentUser);

    IPage<MerchantDto> sortedMerchantsByScore(Integer current, Integer size,  Integer minScore);
}


