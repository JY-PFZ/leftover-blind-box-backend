package nus.iss.se.magicbag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nus.iss.se.magicbag.entity.MagicBag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MagicBagMapper extends BaseMapper<MagicBag> {
    
    @Select("SELECT * FROM magic_bags WHERE merchant_id = #{merchantId}")
    List<MagicBag> findByMerchantId(@Param("merchantId") Integer merchantId);
    
    @Select("SELECT * FROM magic_bags WHERE id = #{id}")
    MagicBag selectById(@Param("id") Integer id);
}
