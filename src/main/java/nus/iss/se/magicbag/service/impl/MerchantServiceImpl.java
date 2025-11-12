package nus.iss.se.magicbag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.auth.common.UserContextHolder;
import nus.iss.se.magicbag.common.constant.MerchantStatus;
import nus.iss.se.magicbag.common.constant.TaskStatus;
import nus.iss.se.magicbag.dto.MerchantDto;
import nus.iss.se.magicbag.dto.MerchantRegisterDto;
import nus.iss.se.magicbag.dto.MerchantUpdateDto;
import nus.iss.se.magicbag.dto.event.MerchantProcessedEvent;
import nus.iss.se.magicbag.dto.event.MerchantRegisterEvent;
import nus.iss.se.magicbag.entity.Merchant;
import nus.iss.se.magicbag.entity.User;
import nus.iss.se.magicbag.mapper.MerchantMapper;
import nus.iss.se.magicbag.mapper.UserMapper;
import nus.iss.se.magicbag.auth.service.UserCacheService;
import nus.iss.se.magicbag.service.IMerchantService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import nus.iss.se.magicbag.common.exception.BusinessException;
import nus.iss.se.magicbag.common.constant.ResultStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper,Merchant> implements IMerchantService {
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final UserContextHolder userContextHolder;
    private final UserCacheService userCacheService;


    /**
     * 实现：获取所有状态为 'approved' 的商家列表
     */
    @Override
    public List<MerchantDto> getAllApprovedMerchants() {
        LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Merchant::getStatus, MerchantStatus.APPROVED.getCode());
        List<Merchant> merchants = list(queryWrapper);
        return merchants.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public MerchantDto getMerchantById(Integer id) {
        Merchant merchant = getById(id);
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
        LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Merchant::getUserId, userId);
        Merchant merchant = getOne(queryWrapper);
        if (merchant == null) {
            log.warn("未找到 user_id 为 {} 的商家记录", userId);
            return null;
        }
        return convertToDto(merchant);
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

        if (StringUtils.isNotBlank(merchantDto.getPhone())) {
            Merchant existingMerchantWithPhone = baseMapper.findByPhone(merchantDto.getPhone());
            if (existingMerchantWithPhone != null && !Objects.equals(existingMerchantWithPhone.getId(), merchantId)) {
                throw new BusinessException(ResultStatus.USER_HAS_EXISTED, "手机号已被其他商家使用");
            }
        }

        Merchant existingMerchant = getById(merchantId);
        if (existingMerchant == null) {
            // 使用 ResultStatus.java 中添加的 MERCHANT_NOT_FOUND
            throw new BusinessException(ResultStatus.MERCHANT_NOT_FOUND, "商家信息不存在，无法更新");
        }
        LambdaUpdateWrapper<Merchant> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Merchant::getId,existingMerchant.getId())
                .set(StringUtils.isNotBlank(merchantDto.getName()), Merchant::getName, merchantDto.getName())
                .set(StringUtils.isNotBlank(merchantDto.getPhone()), Merchant::getPhone, merchantDto.getPhone())
                .set(StringUtils.isNotBlank(merchantDto.getBusinessLicense()), Merchant::getBusinessLicense, merchantDto.getBusinessLicense())
                .set(StringUtils.isNotBlank(merchantDto.getAddress()), Merchant::getAddress, merchantDto.getAddress())
                .set(merchantDto.getLatitude() != null, Merchant::getLatitude, merchantDto.getLatitude())
                .set(merchantDto.getLongitude() != null, Merchant::getLongitude, merchantDto.getLongitude());


        update(updateWrapper);
        log.info("商家 ID {} (用户 {}) 更新店铺信息成功", merchantId, currentUser.getUsername());
    }

    @Override
    public IPage<MerchantDto> sortedMerchantsByScore(Integer current, Integer size, Integer minScore) {
        IPage<MerchantDto> page = new Page<>(current,size);
        // 🟢 修复：传回 Integer 类型的 minScore
        return baseMapper.sortedByScore(page, minScore);
    }

    @Override
    @Transactional
    public void registerMerchant(MerchantRegisterDto dto) {
        // 1. 获取当前用户ID
        UserContext currentUser = userContextHolder.getCurrentUser();
        log.debug("MerchantServiceImpl.registerMerchant - UserContext: {}", currentUser);
        if (currentUser == null) {
            log.error("MerchantServiceImpl.registerMerchant - UserContext is null, user not logged in");
            throw new BusinessException(ResultStatus.USER_NOT_LOGGED_IN, "用户未登录");
        }
        Integer currentUserId = currentUser.getId();
        log.debug("MerchantServiceImpl.registerMerchant - Current userId: {}", currentUserId);

        // 2. 检查用户是否已有商家身份（一对一关系）
        Merchant existingMerchant = baseMapper.findByUserId(currentUserId);
        if (existingMerchant != null) {
            throw new BusinessException(ResultStatus.FAIL, "User had registered merchant");
        }

        // 3. 复制DTO数据到实体对象
        Merchant merchant = new Merchant();
        BeanUtils.copyProperties(dto, merchant);
        // 强制设置 userId 和 id，确保不会被 DTO 覆盖
        merchant.setId(null);
        merchant.setUserId(currentUserId);
        merchant.setStatus(MerchantStatus.PENDING.getCode()); // 设置为待处理状态

        // 3.5. 检查手机号是否已被使用（在插入前检查）
        if (StringUtils.isNotBlank(merchant.getPhone())) {
            Merchant existingMerchantWithPhone = baseMapper.findByPhone(merchant.getPhone());
            if (existingMerchantWithPhone != null) {
                // 如果手机号已存在
                log.warn("手机号 {} 已被其他商家使用，用户ID: {}", merchant.getPhone(), currentUserId);
                throw new BusinessException(ResultStatus.USER_HAS_EXISTED, "手机号已被其他商家使用");
            }
        }

        // 4. 保存或更新商家信息
        save(merchant);
        
        MerchantRegisterEvent event = new MerchantRegisterEvent(
                (long) merchant.getUserId(),
                (long) merchant.getId(),
                merchant.getName(),
                merchant.getPhone(),
                merchant.getAddress(),
                merchant.getBusinessLicense(),
                merchant.getLatitude(),
                merchant.getLongitude()
        );
        eventPublisher.publishEvent(event);

        log.info("商家注册申请已提交，用户ID: {}, 商家ID: {}", currentUserId, merchant.getId());
    }

    @Override
    @EventListener
    @Transactional
    public void handleRegisterResult(MerchantProcessedEvent event) {
        log.info("处理商家{}注册结果：{}", event.userId(), event);
        try {
            // 1. 根据userId查找商家信息
            Merchant merchant = getOne(
                    new QueryWrapper<Merchant>().eq("user_id", event.userId())
            );

            if (merchant == null) {
                log.warn("未找到用户ID为{}的商家记录", event.userId());
                return;
            }

            // 2. 根据审批结果更新商家状态
            if (TaskStatus.APPROVED.getCode().equals(event.status())) {
                // 注册通过，更新商家状态为已通过
                merchant.setStatus("approved");
                merchant.setUpdatedAt(new Date());
                merchant.setApprovedAt(new Date());
                updateById(merchant);

                // 3. 更新用户角色为 MERCHANT
                User user = userMapper.selectById(event.userId().intValue());
                if (user != null) {
                    LambdaUpdateWrapper<User> userWrapper = new LambdaUpdateWrapper<>();
                    userWrapper.eq(User::getId, event.userId().intValue())
                            .set(User::getRole, "MERCHANT")
                            .set(User::getUpdatedAt, new Date());
                    userMapper.update(null, userWrapper);

                    // 4. 清除用户缓存，确保下次登录时加载最新角色
                    userCacheService.deleteUserCache(user.getUsername());

                    // 5. 更新缓存为最新信息（包含新角色）
                    User updatedUser = userMapper.selectById(event.userId().intValue());
                    if (updatedUser != null) {
                        UserContext userContext = new UserContext();
                        BeanUtils.copyProperties(updatedUser, userContext);
                        userCacheService.updateCache(userContext);
                    }

                    log.info("用户角色已更新为商家，用户ID: {}, 用户名: {}", event.userId(), user.getUsername());
                } else {
                    log.warn("未找到用户ID为{}的用户记录，无法更新角色", event.userId());
                }

                log.info("商家注册申请已通过，用户ID: {}, 商家ID: {}", event.userId(), merchant.getId());

            } else if (TaskStatus.REJECTED.getCode().equals(event.status())) {
                // 注册拒绝，更新商家状态为已拒绝
                merchant.setStatus("rejected");
                merchant.setUpdatedAt(new Date());
                updateById(merchant);

                log.info("商家注册申请已拒绝，用户ID: {}, 商家ID: {}", event.userId(), merchant.getId());
            } else {
                log.warn("未知的审批状态: {}", event.status());
            }

        } catch (Exception e) {
            log.error("处理商家注册结果时发生异常，用户ID: {}, 异常信息: {}", event.userId(), e.getMessage(), e);
        }
    }

    private MerchantDto convertToDto(Merchant merchant) {
        if (merchant == null) return null;
        MerchantDto dto = new MerchantDto();
        BeanUtils.copyProperties(merchant, dto);
        return dto;
    }
}