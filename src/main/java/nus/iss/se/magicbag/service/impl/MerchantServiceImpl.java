package nus.iss.se.magicbag.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.dto.MerchantDto;
import nus.iss.se.magicbag.dto.MerchantUpdateDto;
import nus.iss.se.magicbag.entity.Merchant;
import nus.iss.se.magicbag.entity.User;
import nus.iss.se.magicbag.mapper.MerchantMapper;
import nus.iss.se.magicbag.mapper.UserMapper;
import nus.iss.se.magicbag.service.IMerchantService;
import nus.iss.se.magicbag.common.exception.BusinessException;
import nus.iss.se.magicbag.common.constant.ResultStatus;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements IMerchantService {
    
    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;
    
    @Override
    public List<MerchantDto> getAllMerchants() {
        List<Merchant> merchants = merchantMapper.findApprovedMerchants();
        return merchants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public MerchantDto getMerchantById(Integer id) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            return null;
        }
        return convertToDto(merchant);
    }
    
    @Override
    public Integer getMerchantIdByUserId(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        
        // 通过手机号关联用户和商家
        Merchant merchant = merchantMapper.findByPhone(user.getPhone());
        return merchant != null ? merchant.getId() : null;
    }
    
    @Override
    @Transactional
    public void updateMerchantProfile(MerchantUpdateDto merchantDto, UserContext currentUser) {
        // 1. 角色验证：确保是商家
        if (!"MERCHANT".equals(currentUser.getRole())) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED, "只有商家可以修改店铺信息");
        }
        
        // 2. 获取商家ID
        Integer merchantId = getMerchantIdByUserId(currentUser.getId());
        if (merchantId == null) {
            throw new BusinessException(ResultStatus.FAIL, "未找到对应的商家信息");
        }
        
        // 3. 权限验证：确保只能修改自己的商家信息
        if (!Objects.equals(merchantId, merchantDto.getId())) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED, "无权限修改他人商家信息");
        }
        
        // 4. 手机号唯一性验证
        if (StringUtils.hasText(merchantDto.getPhone())) {
            Merchant existingMerchant = merchantMapper.findByPhone(merchantDto.getPhone());
            if (existingMerchant != null && !Objects.equals(existingMerchant.getId(), merchantDto.getId())) {
                throw new BusinessException(ResultStatus.USER_HAS_EXISTED, "手机号已被其他商家使用");
            }
        }
        
        // 5. 检查是否需要重新审核（餐馆图片更新不需要重新审核）
        Merchant existingMerchant = merchantMapper.selectById(merchantDto.getId());
        if (existingMerchant == null) {
            throw new BusinessException(ResultStatus.FAIL, "商家信息不存在");
        }
        
        // 餐馆图片更新不需要重新审核，只是记录日志
        if (!Objects.equals(existingMerchant.getBusinessLicense(), merchantDto.getBusinessLicense())) {
            log.info("商家 {} 更新餐馆图片", merchantDto.getId());
        }
        
        // 6. 更新商家信息
        Merchant merchant = new Merchant();
        BeanUtils.copyProperties(merchantDto, merchant);
        merchant.setUpdatedAt(new Date());
        
        // 餐馆图片更新不需要改变状态
        int updated = merchantMapper.updateById(merchant);
        if (updated <= 0) {
            throw new BusinessException(ResultStatus.FAIL, "商家信息更新失败");
        }
        
        log.info("商家 {} 更新店铺信息成功", currentUser.getUsername());
    }
    
    private MerchantDto convertToDto(Merchant merchant) {
        MerchantDto dto = new MerchantDto();
        BeanUtils.copyProperties(merchant, dto);
        return dto;
    }
}
