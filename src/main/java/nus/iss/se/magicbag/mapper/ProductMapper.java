package nus.iss.se.magicbag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nus.iss.se.magicbag.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select("SELECT * FROM products WHERE merchant_id = #{merchantId} AND is_active = 1")
    List<Product> findByMerchantId(@Param("merchantId") Integer merchantId);

    @Select("SELECT * FROM products WHERE category = #{category} AND is_active = 1")
    List<Product> findByCategory(@Param("category") String category);

    @Select("SELECT COUNT(*) FROM products WHERE is_active = 1")
    Long countActiveProducts();
}




