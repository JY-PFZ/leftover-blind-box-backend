package nus.iss.se.magicbag.service.impl;

import nus.iss.se.magicbag.dto.MerchantDto;
import nus.iss.se.magicbag.entity.Merchant;
import nus.iss.se.magicbag.mapper.MerchantMapper;
import nus.iss.se.magicbag.service.IMerchantService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MerchantServiceImpl implements IMerchantService {
    
    @Autowired
    private MerchantMapper merchantMapper;
    
    @Override
    public List<MerchantDto> getAllMerchants() {
        List<Merchant> merchants = merchantMapper.findApprovedMerchants();
        return merchants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public MerchantDto getMerchantById(Long id) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            return null;
        }
        return convertToDto(merchant);
    }
    
    private MerchantDto convertToDto(Merchant merchant) {
        MerchantDto dto = new MerchantDto();
        BeanUtils.copyProperties(merchant, dto);
        return dto;
    }
}
