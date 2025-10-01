package nus.iss.se.magicbag.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.common.constant.ResultStatus;
import nus.iss.se.magicbag.common.exception.BusinessException;
import nus.iss.se.magicbag.dto.*;
import nus.iss.se.magicbag.entity.MagicBag;
import nus.iss.se.magicbag.entity.Merchant;
import nus.iss.se.magicbag.entity.Order;
import nus.iss.se.magicbag.entity.OrderVerification;
import nus.iss.se.magicbag.entity.User;
import nus.iss.se.magicbag.mapper.MagicBagMapper;
import nus.iss.se.magicbag.mapper.MerchantMapper;
import nus.iss.se.magicbag.mapper.OrderMapper;
import nus.iss.se.magicbag.mapper.OrderVerificationMapper;
import nus.iss.se.magicbag.mapper.UserMapper;
import nus.iss.se.magicbag.service.IOrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {
    
    private final OrderMapper orderMapper;
    private final OrderVerificationMapper orderVerificationMapper;
    private final UserMapper userMapper;
    private final MagicBagMapper magicBagMapper;
    private final MerchantMapper merchantMapper;
    
    @Override
    public IPage<OrderDto> getOrders(UserContext currentUser, OrderQueryDto queryDto) {
        String userRole = currentUser.getRole();
        Page<OrderDto> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());
        
        switch (userRole) {
            case "SUPER_ADMIN":
            case "ADMIN":
                // 管理员可以查看所有订单
                return orderMapper.findAllOrders(page);
                
            case "MERCHANT":
                // 商家只能查看自己店铺的订单
                return orderMapper.findByMerchantId(page, currentUser.getId());
                
            case "USER":
                // 用户只能查看自己的订单
                return orderMapper.findByUserId(page, currentUser.getId());
                
            default:
                throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }
    }
    
    @Override
    public OrderDetailResponse getOrderDetail(Integer orderId, UserContext currentUser) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultStatus.ORDER_NOT_FOUND);
        }
        
        String userRole = currentUser.getRole();
        
        // 权限验证
        switch (userRole) {
            case "SUPER_ADMIN":
            case "ADMIN":
                // 管理员可以查看所有订单
                break;
                
            case "MERCHANT":
                // 商家只能查看自己店铺的订单
                MagicBag magicBag = magicBagMapper.selectById(order.getBagId().intValue());
                if (magicBag == null || !magicBag.getMerchantId().equals(currentUser.getId())) {
                    throw new BusinessException(ResultStatus.ACCESS_DENIED);
                }
                break;
                
            case "USER":
                // 用户只能查看自己的订单
                if (!order.getUserId().equals(currentUser.getId())) {
                    throw new BusinessException(ResultStatus.ACCESS_DENIED);
                }
                break;
                
            default:
                throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }
        
        return buildOrderDetailResponse(order);
    }
    
    @Override
    @Transactional
    public void updateOrderStatus(Integer orderId, OrderStatusUpdateDto statusDto, UserContext currentUser) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultStatus.ORDER_NOT_FOUND);
        }
        
        String userRole = currentUser.getRole();
        
        // 权限验证
        if ("MERCHANT".equals(userRole)) {
            // 商家只能管理自己店铺的订单
            MagicBag magicBag = magicBagMapper.selectById(order.getBagId().intValue());
            if (magicBag == null || !magicBag.getMerchantId().equals(currentUser.getId())) {
                throw new BusinessException(ResultStatus.ACCESS_DENIED);
            }
        } else if (!"SUPER_ADMIN".equals(userRole) && !"ADMIN".equals(userRole)) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }
        
        // 更新订单状态
        order.setStatus(statusDto.getStatus());
        order.setUpdatedAt(new Date());
        
        // 根据状态设置相应的时间字段
        switch (statusDto.getStatus()) {
            case "paid":
                order.setPaidAt(new Date());
                break;
            case "completed":
                order.setCompletedAt(new Date());
                break;
            case "cancelled":
                order.setCancelledAt(new Date());
                break;
        }
        
        orderMapper.updateById(order);
    }
    
    @Override
    @Transactional
    public void cancelOrder(Integer orderId, UserContext currentUser) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultStatus.ORDER_NOT_FOUND);
        }
        
        String userRole = currentUser.getRole();
        
        // 权限验证
        if ("USER".equals(userRole)) {
            // 用户只能取消自己的订单
            if (!order.getUserId().equals(currentUser.getId())) {
                throw new BusinessException(ResultStatus.ACCESS_DENIED);
            }
        } else if (!"SUPER_ADMIN".equals(userRole) && !"ADMIN".equals(userRole)) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }
        
        // 检查订单状态
        if ("completed".equals(order.getStatus())) {
            throw new BusinessException(ResultStatus.ORDER_CANNOT_CANCEL);
        }
        if ("cancelled".equals(order.getStatus())) {
            throw new BusinessException(ResultStatus.ORDER_ALREADY_CANCELLED);
        }
        
        // 取消订单
        order.setStatus("cancelled");
        order.setCancelledAt(new Date());
        order.setUpdatedAt(new Date());
        orderMapper.updateById(order);
    }
    
    @Override
    @Transactional
    public void verifyOrder(Integer orderId, OrderVerificationDto verificationDto, UserContext currentUser) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultStatus.ORDER_NOT_FOUND);
        }
        
        // 只有商家可以核销订单
        if (!"MERCHANT".equals(currentUser.getRole())) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }
        
        // 验证商家权限
        MagicBag magicBag = magicBagMapper.selectById(order.getBagId().intValue());
        if (magicBag == null || !magicBag.getMerchantId().equals(currentUser.getId())) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }
        
        // 检查订单状态
        if (!"paid".equals(order.getStatus())) {
            throw new BusinessException(ResultStatus.ORDER_CANNOT_VERIFY);
        }
        
        // 创建核销记录
        OrderVerification verification = new OrderVerification();
        verification.setOrderId(orderId.intValue());
        verification.setVerifiedBy(currentUser.getId());
        verification.setVerifiedAt(new Date());
        verification.setLocation(verificationDto.getLocation());
        orderVerificationMapper.insert(verification);
        
        // 更新订单状态为已完成
        order.setStatus("completed");
        order.setCompletedAt(new Date());
        order.setUpdatedAt(new Date());
        orderMapper.updateById(order);
    }
    
    @Override
    public OrderStatsDto getOrderStats(UserContext currentUser) {
        String userRole = currentUser.getRole();
        
        switch (userRole) {
            case "SUPER_ADMIN":
            case "ADMIN":
                return orderMapper.findAllOrderStats();
                
            case "MERCHANT":
                return orderMapper.findOrderStatsByMerchantId(currentUser.getId());
                
            case "USER":
                return orderMapper.findOrderStatsByUserId(currentUser.getId());
                
            default:
                throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }
    }
    
    /**
     * 构建订单详情响应
     */
    private OrderDetailResponse buildOrderDetailResponse(Order order) {
        OrderDetailResponse response = new OrderDetailResponse();
        
        // 构建订单基本信息
        OrderDto orderDto = new OrderDto();
        BeanUtils.copyProperties(order, orderDto);
        response.setOrder(orderDto);
        
        // 查询用户信息
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            OrderDetailResponse.UserInfo userInfo = new OrderDetailResponse.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setNickname(user.getNickname());
            userInfo.setPhone(user.getPhone());
            response.setUser(userInfo);
        }
        
        // 查询魔法袋信息
        MagicBag magicBag = magicBagMapper.selectById(order.getBagId().intValue());
        if (magicBag != null) {
            OrderDetailResponse.MagicBagInfo bagInfo = new OrderDetailResponse.MagicBagInfo();
            bagInfo.setId(magicBag.getId());
            bagInfo.setTitle(magicBag.getTitle());
            bagInfo.setDescription(magicBag.getDescription());
            bagInfo.setCategory(magicBag.getCategory());
            bagInfo.setImageUrl(magicBag.getImageUrl());
            response.setMagicBag(bagInfo);
            
            // 查询商家信息
            Merchant merchant = merchantMapper.selectById(magicBag.getMerchantId());
            if (merchant != null) {
                OrderDetailResponse.MerchantInfo merchantInfo = new OrderDetailResponse.MerchantInfo();
                merchantInfo.setId(merchant.getId());
                merchantInfo.setName(merchant.getName());
                merchantInfo.setPhone(merchant.getPhone());
                merchantInfo.setAddress(merchant.getAddress());
                response.setMerchant(merchantInfo);
            }
        }
        
        // 查询核销记录
        List<OrderVerification> verifications = orderVerificationMapper.findByOrderId(order.getId().intValue());
        List<OrderVerificationDto> verificationDtos = verifications.stream()
                .map(this::convertToVerificationDto)
                .collect(Collectors.toList());
        response.setVerifications(verificationDtos);
        
        return response;
    }
    
    /**
     * 转换核销记录为DTO
     */
    private OrderVerificationDto convertToVerificationDto(OrderVerification verification) {
        OrderVerificationDto dto = new OrderVerificationDto();
        BeanUtils.copyProperties(verification, dto);
        return dto;
    }
}
