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
import nus.iss.se.magicbag.auth.service.UserCacheService;
import nus.iss.se.magicbag.service.IMerchantService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
    private final UserCacheService userCacheService;


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
        
        // 强制验证 currentUserId 不为 null
        if (currentUserId == null || currentUserId <= 0) {
            log.error("currentUserId 无效，用户ID: {}", currentUserId);
            throw new BusinessException(ResultStatus.USER_NOT_LOGGED_IN, "用户ID无效，请重新登录");
        }

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
        // 强制设置 userId 和 id，确保不会被 DTO 覆盖
        merchant.setUserId(currentUserId); // 强制设置，currentUserId 已确保不为 null
        // 如果是新记录，确保 id 为 null（由数据库自动生成）
        if (existingMerchant == null) {
            merchant.setId(null);
        }
        merchant.setStatus("pending"); // 设置为待处理状态
        merchant.setCreatedAt(new Date());
        merchant.setUpdatedAt(new Date());
        
        // 强制验证 userId 已正确设置（多重验证）
        Integer verifyUserId = merchant.getUserId();
        if (verifyUserId == null || verifyUserId <= 0) {
            log.error("userId 设置失败或无效，currentUserId: {}, merchant.getUserId(): {}", currentUserId, verifyUserId);
            // 强制重新设置
            merchant.setUserId(currentUserId);
            verifyUserId = merchant.getUserId();
            if (verifyUserId == null || verifyUserId <= 0) {
                throw new BusinessException(ResultStatus.FAIL, "用户ID设置失败，无法继续注册");
            }
            log.warn("强制重新设置 userId 成功: {}", verifyUserId);
        }

        // 4. 保存或更新商家信息
        if (existingMerchant != null) {
            merchantMapper.updateById(merchant);
        } else {
            String merchantName = merchant.getName();
            
            merchantMapper.insert(merchant);
            
            // 如果 MyBatis-Plus 没有自动填充 id，尝试多种方式获取
            if (merchant.getId() == null) {
                log.warn("商家插入后 id 未自动填充，尝试获取，用户ID: {}", currentUserId);
                
                // 方法1: 使用 LAST_INSERT_ID()
                Integer lastInsertId = merchantMapper.getLastInsertId();
                if (lastInsertId != null && lastInsertId > 0) {
                    merchant.setId(lastInsertId);
                    log.info("通过 LAST_INSERT_ID() 成功获取商家ID: {}", merchant.getId());
                } else {
                    log.warn("LAST_INSERT_ID() 失败，尝试通过查询获取");
                }
                
                // 方法2: 如果 LAST_INSERT_ID() 失败或仍然为 null，通过查询获取
                if (merchant.getId() == null) {
                    Merchant insertedMerchant = merchantMapper.selectOne(
                        new QueryWrapper<Merchant>()
                            .eq("user_id", currentUserId)
                            .eq("name", merchantName)
                            .eq("status", "pending")
                            .orderByDesc("created_at")
                            .last("LIMIT 1")
                    );
                    
                    if (insertedMerchant != null && insertedMerchant.getId() != null) {
                        merchant.setId(insertedMerchant.getId());
                        log.info("通过查询成功获取商家ID: {}", merchant.getId());
                    } else {
                        // 查询失败，但不抛出异常，让后面的兜底方案处理
                        log.warn("查询失败，将在后续步骤中处理，用户ID: {}, 商家名称: {}", currentUserId, merchantName);
                    }
                }
            }
        }

        // 5. 发布商家注册事件前，再次确保 merchantId 有值
        Integer merchantId = merchant.getId();
        if (merchantId == null) {
            // 最后尝试：如果还是 null，强制查询一次
            log.error("商家ID仍然为空，强制查询，用户ID: {}", currentUserId);
            Merchant finalMerchant = merchantMapper.selectOne(
                new QueryWrapper<Merchant>()
                    .eq("user_id", currentUserId)
                    .eq("status", "pending")
                    .orderByDesc("created_at")
                    .last("LIMIT 1")
            );
            
            if (finalMerchant != null && finalMerchant.getId() != null) {
                merchant.setId(finalMerchant.getId());
                merchantId = finalMerchant.getId();
                log.info("强制查询后成功获取商家ID: {}", merchantId);
            } else {
                // 如果所有方法都失败，使用临时ID（使用userId作为占位符，或者使用0）
                // 注意：这只是一个临时解决方案，事件监听器可能需要处理这种情况
                log.error("所有方法都无法获取商家ID，使用临时ID，用户ID: {}", currentUserId);
                
                // 确保 merchantId 不为 null：如果 currentUserId 不为 null 就用它，否则使用 0 作为占位符
                if (currentUserId != null && currentUserId > 0) {
                    merchantId = currentUserId; // 临时使用 userId 作为 merchantId
                } else {
                    merchantId = 0; // 如果 userId 也是 null，使用 0 作为占位符
                    log.warn("⚠️ userId 也为 null，使用 0 作为临时 merchantId");
                }
                
                merchant.setId(merchantId);
                log.warn("⚠️ 使用临时ID作为 merchantId: {}", merchantId);
            }
        }
        
        // 确保 userId 不为 null（currentUserId 已在前面验证过不为 null）
        Integer userId = merchant.getUserId();
        if (userId == null || userId <= 0) {
            log.error("发布事件时 userId 为空或无效，强制使用 currentUserId: {}", currentUserId);
            userId = currentUserId; // currentUserId 已确保不为 null 且 > 0
        }
        
        MerchantRegisterEvent event = new MerchantRegisterEvent(
                (long) userId,
                (long) merchantId,
                merchant.getName(),
                merchant.getPhone(),
                merchant.getAddress(),
                merchant.getBusinessLicense(),
                merchant.getLatitude(),
                merchant.getLongitude()
        );
        eventPublisher.publishEvent(event);

        log.info("商家注册申请已提交，用户ID: {}, 商家ID: {}", currentUserId, merchantId);
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