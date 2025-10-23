package nus.iss.se.magicbag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nus.iss.se.magicbag.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select; // 导入 Select 注解
import java.util.List; // 导入 List

/**
 * OrderItem 数据访问层接口
 * 继承 BaseMapper 以获得标准的 CRUD 功能
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 根据订单ID查询订单明细列表
     * (虽然 BaseMapper 提供了 selectList，但显式声明一个更清晰)
     * @param orderId 订单ID
     * @return 订单明细列表
     */
    @Select("SELECT * FROM order_items WHERE order_id = #{orderId}")
    List<OrderItem> findByOrderId(Integer orderId);

}
