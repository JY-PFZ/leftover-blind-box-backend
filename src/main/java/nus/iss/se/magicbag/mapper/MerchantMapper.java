package nus.iss.se.magicbag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nus.iss.se.magicbag.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {

    @Select("SELECT * FROM merchants WHERE status = 'approved'")
    List<Merchant> findApprovedMerchants();
}


