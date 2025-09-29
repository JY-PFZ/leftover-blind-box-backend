package nus.iss.se.magicbag.controller;

import nus.iss.se.magicbag.common.Result;
import nus.iss.se.magicbag.dto.MerchantDto;
import nus.iss.se.magicbag.service.IMerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchants")
@CrossOrigin(origins = "*")
public class MerchantController {
    
    @Autowired
    private IMerchantService merchantService;
    
    /**
     * 获取所有已审核的商家列表
     */
    @GetMapping
    public Result<List<MerchantDto>> getAllMerchants() {
        List<MerchantDto> merchants = merchantService.getAllMerchants();
        return Result.success(merchants);
    }
    
    /**
     * 根据ID获取商家详情
     */
    @GetMapping("/{id}")
    public Result<MerchantDto> getMerchantById(@PathVariable Long id) {
        MerchantDto merchant = merchantService.getMerchantById(id);
        if (merchant == null) {
            return Result.error("商家不存在");
        }
        return Result.success(merchant);
    }
}
