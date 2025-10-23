package nus.iss.se.magicbag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nus.iss.se.magicbag.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    
    @Select("SELECT * FROM order_items WHERE order_id = #{orderId}")
    List<OrderItem> findByOrderId(@Param("orderId") Integer orderId);
    
    @Select("SELECT COUNT(*) FROM order_items WHERE order_id = #{orderId}")
    Integer countByOrderId(@Param("orderId") Integer orderId);
}
