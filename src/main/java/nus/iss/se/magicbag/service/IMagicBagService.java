package nus.iss.se.magicbag.service;

import com.baomidou.mybatisplus.extension.service.IService;
import nus.iss.se.magicbag.dto.MagicBagCreateDto;
import nus.iss.se.magicbag.dto.MagicBagDto;
import nus.iss.se.magicbag.dto.MagicBagListResponse;
import nus.iss.se.magicbag.dto.MagicBagUpdateDto;
import nus.iss.se.magicbag.entity.MagicBag;
import nus.iss.se.magicbag.entity.User;

import java.util.List;

public interface IMagicBagService extends IService<MagicBag> {
    
    /**
     * 获取所有盲盒列表（分页）
     */
    MagicBagListResponse getAllMagicBags(Integer page, Integer size);
    
    /**
     * 根据ID获取盲盒详情
     */
    MagicBagDto getMagicBagById(Integer id);
    
    /**
     * 根据分类获取盲盒
     */
    List<MagicBagDto> getMagicBagsByCategory(String category);
    
    /**
     * 根据商家ID获取盲盒
     */
    List<MagicBagDto> getMagicBagsByMerchantId(Integer merchantId);
    
    /**
     * 创建新的盲盒商品
     */
    MagicBagDto createMagicBag(MagicBagCreateDto createDto);
    
    /**
     * 更新盲盒商品信息
     */
    MagicBagDto updateMagicBag(Integer id, MagicBagUpdateDto updateDto);
    
    /**
     * 删除盲盒商品
     */
    boolean deleteMagicBag(Integer id);
}
