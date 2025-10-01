package nus.iss.se.magicbag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import nus.iss.se.magicbag.dto.OrderDto;
import nus.iss.se.magicbag.dto.OrderStatsDto;
import nus.iss.se.magicbag.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    /**
     * 管理员查询所有订单
     */
    @Select("SELECT o.*, u.nickname as user_name, mb.title as bag_title, m.name as merchant_name " +
            "FROM orders o " +
            "LEFT JOIN users u ON o.user_id = u.id " +
            "LEFT JOIN magic_bags mb ON o.bag_id = mb.id " +
            "LEFT JOIN merchants m ON mb.merchant_id = m.id " +
            "ORDER BY o.created_at DESC")
    IPage<OrderDto> findAllOrders(Page<OrderDto> page);
    
    /**
     * 商家查询自己店铺的订单
     */
    @Select("SELECT o.*, u.nickname as user_name, mb.title as bag_title " +
            "FROM orders o " +
            "LEFT JOIN users u ON o.user_id = u.id " +
            "LEFT JOIN magic_bags mb ON o.bag_id = mb.id " +
            "WHERE mb.merchant_id = #{merchantId} " +
            "ORDER BY o.created_at DESC")
    IPage<OrderDto> findByMerchantId(Page<OrderDto> page, @Param("merchantId") Integer merchantId);
    
    /**
     * 用户查询自己的订单
     */
    @Select("SELECT o.*, mb.title as bag_title, m.name as merchant_name " +
            "FROM orders o " +
            "LEFT JOIN magic_bags mb ON o.bag_id = mb.id " +
            "LEFT JOIN merchants m ON mb.merchant_id = m.id " +
            "WHERE o.user_id = #{userId} " +
            "ORDER BY o.created_at DESC")
    IPage<OrderDto> findByUserId(Page<OrderDto> page, @Param("userId") Integer userId);
    
    /**
     * 查询订单统计信息
     */
    @Select("SELECT COUNT(*) as total_orders, " +
            "COALESCE(SUM(total_price), 0) as total_amount, " +
            "SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END) as pending_orders, " +
            "SUM(CASE WHEN status = 'paid' THEN 1 ELSE 0 END) as paid_orders, " +
            "SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completed_orders, " +
            "SUM(CASE WHEN status = 'cancelled' THEN 1 ELSE 0 END) as cancelled_orders " +
            "FROM orders WHERE user_id = #{userId}")
    OrderStatsDto findOrderStatsByUserId(@Param("userId") Integer userId);
    
    /**
     * 查询商家订单统计信息
     */
    @Select("SELECT COUNT(*) as total_orders, " +
            "COALESCE(SUM(o.total_price), 0) as total_amount, " +
            "SUM(CASE WHEN o.status = 'pending' THEN 1 ELSE 0 END) as pending_orders, " +
            "SUM(CASE WHEN o.status = 'paid' THEN 1 ELSE 0 END) as paid_orders, " +
            "SUM(CASE WHEN o.status = 'completed' THEN 1 ELSE 0 END) as completed_orders, " +
            "SUM(CASE WHEN o.status = 'cancelled' THEN 1 ELSE 0 END) as cancelled_orders " +
            "FROM orders o LEFT JOIN magic_bags mb ON o.bag_id = mb.id " +
            "WHERE mb.merchant_id = #{merchantId}")
    OrderStatsDto findOrderStatsByMerchantId(@Param("merchantId") Integer merchantId);
    
    /**
     * 查询所有订单统计信息（管理员）
     */
    @Select("SELECT COUNT(*) as total_orders, " +
            "COALESCE(SUM(total_price), 0) as total_amount, " +
            "SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END) as pending_orders, " +
            "SUM(CASE WHEN status = 'paid' THEN 1 ELSE 0 END) as paid_orders, " +
            "SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completed_orders, " +
            "SUM(CASE WHEN status = 'cancelled' THEN 1 ELSE 0 END) as cancelled_orders " +
            "FROM orders")
    OrderStatsDto findAllOrderStats();
}
