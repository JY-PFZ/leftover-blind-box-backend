package nus.iss.se.magicbag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import nus.iss.se.magicbag.dto.MerchantDto;
import nus.iss.se.magicbag.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {
    
    /**
     * 查询所有已审核的商家
     */
    @Select("SELECT * FROM merchants WHERE status = 'approved' ORDER BY created_at DESC")
    List<Merchant> findApprovedMerchants();
    
    /**
     * 根据手机号查找商家
     */
    @Select("SELECT * FROM merchants WHERE phone = #{phone}")
    Merchant findByPhone(@Param("phone") String phone);

    @Select("SELECT id,name,score FROM merchants WHERE status = 'approved' and score >= #{miScore} ORDER BY score DESC")
    IPage<MerchantDto> sortedByScore(IPage<MerchantDto> page, @Param("minScore") Integer minScore);
}



