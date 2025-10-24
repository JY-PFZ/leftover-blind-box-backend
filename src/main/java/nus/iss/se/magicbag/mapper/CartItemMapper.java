package nus.iss.se.magicbag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nus.iss.se.magicbag.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * CartItem 数据访问层接口
 * 核心改动:
 * 1. 继承 BaseMapper<CartItem> 来获得所有标准的 CRUD 方法。
 * 2. MyBatis-Plus 会确保所有自动生成的方法 (如 deleteById) 都能正确使用
 * CartItem 实体类中 @TableId 注解的列名映射。
 * 3. 不再需要手动编写任何简单的 SQL 语句。
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
    // 继承 BaseMapper 后，此接口通常保持为空。
    // 所有基础的增、删、改、查功能都已经自动拥有。
}

