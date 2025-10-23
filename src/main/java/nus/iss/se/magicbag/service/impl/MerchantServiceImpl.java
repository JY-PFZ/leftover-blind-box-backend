package nus.iss.se.magicbag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
// import org.springframework.beans.factory.annotation.Qualifier; // 移除未使用的 import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MerchantServiceImpl implements IMerchantService {

    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;

    // 构造函数注入
    public MerchantServiceImpl(MerchantMapper merchantMapper, UserMapper userMapper) {
        this.merchantMapper = merchantMapper;
        this.userMapper = userMapper;
    }

    /**
     * @deprecated 请使用 getAllApprovedMerchants
     */
    @Override
    @Deprecated
    public List<MerchantDto> getAllMerchants() {
        // 保留旧实现，假设 findApprovedMerchants 存在
        List<Merchant> merchants = merchantMapper.findApprovedMerchants();
        return merchants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 实现：获取所有状态为 'approved' 的商家列表
     */
    @Override
    public List<MerchantDto> getAllApprovedMerchants() {
        QueryWrapper<Merchant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "approved");
        List<Merchant> merchants = merchantMapper.selectList(queryWrapper);
        return merchants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public MerchantDto getMerchantById(Integer id) {
        Merchant merchant = merchantMapper.selectById(id);
        return convertToDto(merchant); // convertToDto 内部处理 null
    }

    /**
     * 实现：根据用户ID查找商家信息
     */
    @Override
    public MerchantDto findByUserId(Integer userId) {
        if (userId == null) {
            return null;
        }
        QueryWrapper<Merchant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        Merchant merchant = merchantMapper.selectOne(queryWrapper);
        if (merchant == null) {
            log.warn("未找到 user_id 为 {} 的商家记录", userId);
            return null;
        }
        return convertToDto(merchant);
    }

    /**
     * @deprecated 建议使用 findByUserId 获取完整 DTO
     */
    @Override
    @Deprecated
    public Integer getMerchantIdByUserId(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !StringUtils.hasText(user.getPhone())) {
            log.warn("无法通过 phone 关联商家：用户 {} 不存在或没有手机号", userId);
            return null;
        }

        Merchant merchant = merchantMapper.findByPhone(user.getPhone());
        if (merchant == null) {
            log.warn("未找到手机号为 {} 的商家记录 (关联用户 {})", user.getPhone(), userId);
        }
        return merchant != null ? merchant.getId() : null;
    }

    @Override
    @Transactional
    public void updateMerchantProfile(MerchantUpdateDto merchantDto, UserContext currentUser) {
        String userRole = currentUser.getRole(); // 假设 UserContextHolder 现在填充了 Role

        if (!"MERCHANT".equalsIgnoreCase(userRole)) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED, "只有商家可以修改店铺信息");
        }

        MerchantDto currentMerchantDto = findByUserId(currentUser.getId());
        if (currentMerchantDto == null) {
            // 使用 ResultStatus.java 中添加的 MERCHANT_NOT_FOUND
            throw new BusinessException(ResultStatus.MERCHANT_NOT_FOUND, "未找到当前用户关联的商家信息");
        }
        Integer merchantId = currentMerchantDto.getId();

        if (merchantDto.getId() != null && !Objects.equals(merchantId, merchantDto.getId())) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED, "无权限修改他人商家信息");
        }

        if (StringUtils.hasText(merchantDto.getPhone())) {
            Merchant existingMerchantWithPhone = merchantMapper.findByPhone(merchantDto.getPhone());
            if (existingMerchantWithPhone != null && !Objects.equals(existingMerchantWithPhone.getId(), merchantId)) {
                throw new BusinessException(ResultStatus.USER_HAS_EXISTED, "手机号已被其他商家使用");
            }
        }

        Merchant existingMerchant = merchantMapper.selectById(merchantId);
        if (existingMerchant == null) {
            // 使用 ResultStatus.java 中添加的 MERCHANT_NOT_FOUND
            throw new BusinessException(ResultStatus.MERCHANT_NOT_FOUND, "商家信息不存在，无法更新");
        }

        // 手动选择性更新:
        if (merchantDto.getName() != null) existingMerchant.setName(merchantDto.getName());
        if (merchantDto.getPhone() != null) existingMerchant.setPhone(merchantDto.getPhone());
        if (merchantDto.getBusinessLicense() != null) existingMerchant.setBusinessLicense(merchantDto.getBusinessLicense());
        if (merchantDto.getAddress() != null) existingMerchant.setAddress(merchantDto.getAddress());
        if (merchantDto.getLatitude() != null) existingMerchant.setLatitude(merchantDto.getLatitude());
        if (merchantDto.getLongitude() != null) existingMerchant.setLongitude(merchantDto.getLongitude());

        existingMerchant.setUpdatedAt(new Date());

        int updated = merchantMapper.updateById(existingMerchant);
        if (updated <= 0) {
            throw new BusinessException(ResultStatus.FAIL, "商家信息更新失败");
        }

        log.info("商家 ID {} (用户 {}) 更新店铺信息成功", merchantId, currentUser.getUsername());
    }

    @Override
    public IPage<MerchantDto> sortedMerchantsByScore(Integer current, Integer size, Integer minScore) {
        IPage<MerchantDto> page = new Page<>(current,size);
        // 🟢 修复：传回 Integer 类型的 minScore
        return merchantMapper.sortedByScore(page, minScore);
    }

    private MerchantDto convertToDto(Merchant merchant) {
        if (merchant == null) return null;
        MerchantDto dto = new MerchantDto();
        BeanUtils.copyProperties(merchant, dto);
        return dto;
    }
}
