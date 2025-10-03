package nus.iss.se.magicbag.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.dto.*;

public interface IOrderService {
    
    /**
     * 根据用户角色获取订单列表
     */
    IPage<OrderDto> getOrders(UserContext currentUser, OrderQueryDto queryDto);
    
    /**
     * 获取订单详情
     */
    OrderDetailResponse getOrderDetail(Integer orderId, UserContext currentUser);
    
    /**
     * 更新订单状态
     */
    void updateOrderStatus(Integer orderId, OrderStatusUpdateDto statusDto, UserContext currentUser);
    
    /**
     * 取消订单
     */
    void cancelOrder(Integer orderId, UserContext currentUser);
    
    /**
     * 核销订单
     */
    void verifyOrder(Integer orderId, OrderVerificationDto verificationDto, UserContext currentUser);
    
    /**
     * 获取订单统计信息
     */
    OrderStatsDto getOrderStats(UserContext currentUser);
}

