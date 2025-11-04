package nus.iss.se.magicbag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.auth.common.UserContextHolder;
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
import nus.iss.se.magicbag.service.IMerchantService;
import nus.iss.se.magicbag.common.exception.BusinessException;
import nus.iss.se.magicbag.common.constant.ResultStatus;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
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
    private final ApplicationEventPublisher eventPublisher;
    private final UserContextHolder userContextHolder;


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
        Merchant existingMerchant = merchantMapper.selectOne(
            new QueryWrapper<Merchant>().eq("user_id", currentUserId)
        );

        Merchant merchant;
        if (existingMerchant != null) {
            // 如果已存在，更新现有记录
            merchant = existingMerchant;
            log.info("用户{}已有商家记录，将更新现有信息", currentUserId);
        } else {
            // 创建新的商家记录
            merchant = new Merchant();
        }

        // 3. 复制DTO数据到实体对象
        BeanUtils.copyProperties(dto, merchant);
        merchant.setUserId(currentUserId);
        merchant.setStatus("pending"); // 设置为待处理状态
        merchant.setCreatedAt(new Date());
        merchant.setUpdatedAt(new Date());

        // 4. 保存或更新商家信息
        if (existingMerchant != null) {
            merchantMapper.updateById(merchant);
        } else {
            merchantMapper.insert(merchant);
        }

        // 5. 发布商家注册事件
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
            Merchant merchant = merchantMapper.selectOne(
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
                merchantMapper.updateById(merchant);

                log.info("商家注册申请已通过，用户ID: {}, 商家ID: {}", event.userId(), merchant.getId());

            } else if (TaskStatus.REJECTED.getCode().equals(event.status())) {
                // 注册拒绝，更新商家状态为已拒绝
                merchant.setStatus("rejected");
                merchant.setUpdatedAt(new Date());
                merchantMapper.updateById(merchant);

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
