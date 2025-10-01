package nus.iss.se.magicbag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nus.iss.se.magicbag.entity.OrderVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderVerificationMapper extends BaseMapper<OrderVerification> {
    
    /**
     * 根据订单ID查询核销记录
     */
    @Select("SELECT ov.*, u.nickname as verifier_name " +
            "FROM order_verifications ov " +
            "LEFT JOIN users u ON ov.verified_by = u.id " +
            "WHERE ov.order_id = #{orderId} " +
            "ORDER BY ov.verified_at DESC")
    List<OrderVerification> findByOrderId(@Param("orderId") Integer orderId);
    
    /**
     * 根据核销人ID查询核销记录
     */
    @Select("SELECT ov.*, o.order_no " +
            "FROM order_verifications ov " +
            "LEFT JOIN orders o ON ov.order_id = o.id " +
            "WHERE ov.verified_by = #{verifiedBy} " +
            "ORDER BY ov.verified_at DESC")
    List<OrderVerification> findByVerifiedBy(@Param("verifiedBy") Integer verifiedBy);
}
