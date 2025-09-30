package nus.iss.se.magicbag.service;

import nus.iss.se.magicbag.dto.MerchantDto;

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
}


