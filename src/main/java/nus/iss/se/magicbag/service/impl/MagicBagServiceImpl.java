package nus.iss.se.magicbag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import nus.iss.se.magicbag.dto.MagicBagDto;
import nus.iss.se.magicbag.dto.MagicBagListResponse;
import nus.iss.se.magicbag.entity.MagicBag;
import nus.iss.se.magicbag.mapper.MagicBagMapper;
import nus.iss.se.magicbag.service.IMagicBagService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MagicBagServiceImpl implements IMagicBagService {
    
    @Autowired
    private MagicBagMapper magicBagMapper;
    
    @Override
    public MagicBagListResponse getAllMagicBags(Integer page, Integer size) {
        Page<MagicBag> magicBagPage = new Page<>(page, size);
        QueryWrapper<MagicBag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_active", true);
        
        Page<MagicBag> result = magicBagMapper.selectPage(magicBagPage, queryWrapper);
        
        List<MagicBagDto> magicBagDtos = result.getRecords().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        MagicBagListResponse response = new MagicBagListResponse();
        response.setMagicBags(magicBagDtos);
        response.setTotalItems(result.getTotal());
        response.setCurrentPage((int) result.getCurrent());
        response.setPageSize((int) result.getSize());
        response.setTotalPages((int) result.getPages());
        
        return response;
    }
    
    @Override
    public MagicBagDto getMagicBagById(Integer id) {
        MagicBag magicBag = magicBagMapper.selectById(id);
        if (magicBag == null) {
            return null;
        }
        return convertToDto(magicBag);
    }
    
    @Override
    public List<MagicBagDto> getMagicBagsByCategory(String category) {
        List<MagicBag> magicBags = magicBagMapper.findByCategory(category);
        return magicBags.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MagicBagDto> getMagicBagsByMerchantId(Integer merchantId) {
        List<MagicBag> magicBags = magicBagMapper.findByMerchantId(merchantId);
        return magicBags.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private MagicBagDto convertToDto(MagicBag magicBag) {
        MagicBagDto dto = new MagicBagDto();
        BeanUtils.copyProperties(magicBag, dto);
        return dto;
    }
}
