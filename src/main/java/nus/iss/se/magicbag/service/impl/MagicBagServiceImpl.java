package nus.iss.se.magicbag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import nus.iss.se.magicbag.dto.MagicBagCreateDto;
import nus.iss.se.magicbag.dto.MagicBagDto;
import nus.iss.se.magicbag.dto.MagicBagListResponse;
import nus.iss.se.magicbag.dto.MagicBagUpdateDto;
import nus.iss.se.magicbag.entity.MagicBag;
import nus.iss.se.magicbag.mapper.MagicBagMapper;
import nus.iss.se.magicbag.service.IMagicBagService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Override
    @Transactional
    public MagicBagDto createMagicBag(MagicBagCreateDto createDto) {
        MagicBag magicBag = new MagicBag();
        BeanUtils.copyProperties(createDto, magicBag);
        
        // 设置默认值
        magicBag.setActive(true);
        magicBag.setCreatedAt(LocalDateTime.now());
        magicBag.setUpdatedAt(LocalDateTime.now());
        
        magicBagMapper.insert(magicBag);
        
        return convertToDto(magicBag);
    }

    @Override
    @Transactional
    public MagicBagDto updateMagicBag(Integer id, MagicBagUpdateDto updateDto) {
        MagicBag existingMagicBag = magicBagMapper.selectById(id);
        if (existingMagicBag == null) {
            throw new RuntimeException("盲盒不存在");
        }
        
        // 只更新非空字段
        if (updateDto.getTitle() != null) {
            existingMagicBag.setTitle(updateDto.getTitle());
        }
        if (updateDto.getDescription() != null) {
            existingMagicBag.setDescription(updateDto.getDescription());
        }
        if (updateDto.getPrice() != null) {
            existingMagicBag.setPrice(updateDto.getPrice());
        }
        if (updateDto.getQuantity() != null) {
            existingMagicBag.setQuantity(updateDto.getQuantity());
        }
        if (updateDto.getPickupStartTime() != null) {
            existingMagicBag.setPickupStartTime(updateDto.getPickupStartTime());
        }
        if (updateDto.getPickupEndTime() != null) {
            existingMagicBag.setPickupEndTime(updateDto.getPickupEndTime());
        }
        if (updateDto.getAvailableDate() != null) {
            existingMagicBag.setAvailableDate(updateDto.getAvailableDate());
        }
        if (updateDto.getCategory() != null) {
            existingMagicBag.setCategory(updateDto.getCategory());
        }
        if (updateDto.getImageUrl() != null) {
            existingMagicBag.setImageUrl(updateDto.getImageUrl());
        }
        if (updateDto.getIsActive() != null) {
            existingMagicBag.setActive(updateDto.getIsActive());
        }
        
        existingMagicBag.setUpdatedAt(LocalDateTime.now());
        
        magicBagMapper.updateById(existingMagicBag);
        
        return convertToDto(existingMagicBag);
    }

    @Override
    @Transactional
    public boolean deleteMagicBag(Integer id) {
        MagicBag magicBag = magicBagMapper.selectById(id);
        if (magicBag == null) {
            return false;
        }
        
        // 软删除：将is_active设置为false
        magicBag.setActive(false);
        magicBag.setUpdatedAt(LocalDateTime.now());
        
        int result = magicBagMapper.updateById(magicBag);
        return result > 0;
    }

    private MagicBagDto convertToDto(MagicBag magicBag) {
        if (magicBag == null) {
            return null;
        }
        MagicBagDto dto = new MagicBagDto();
        // 首先，自动复制所有其他名称和类型匹配的字段
        BeanUtils.copyProperties(magicBag, dto);

        // 手动处理 price 字段的类型转换 (从 Float 到 BigDecimal)
        dto.setPrice(BigDecimal.valueOf(magicBag.getPrice()));

        return dto;
    }
}
