package nus.iss.se.magicbag.controller;

import nus.iss.se.magicbag.common.Result;
import nus.iss.se.magicbag.dto.MagicBagDto;
import nus.iss.se.magicbag.dto.MagicBagListResponse;
import nus.iss.se.magicbag.service.IMagicBagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/magic-bags")
@CrossOrigin(origins = "*")
public class MagicBagController {
    
    @Autowired
    private IMagicBagService magicBagService;
    
    /**
     * 获取所有盲盒列表（分页）
     */
    @GetMapping
    public Result<MagicBagListResponse> getAllMagicBags(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        MagicBagListResponse response = magicBagService.getAllMagicBags(page, size);
        return Result.success(response);
    }
    
    /**
     * 根据ID获取盲盒详情
     */
    @GetMapping("/{id}")
    public Result<MagicBagDto> getMagicBagById(@PathVariable Integer id) {
        MagicBagDto magicBag = magicBagService.getMagicBagById(id);
        if (magicBag == null) {
            return Result.error("盲盒不存在");
        }
        return Result.success(magicBag);
    }
    
    /**
     * 根据分类获取盲盒
     */
    @GetMapping("/category/{category}")
    public Result<List<MagicBagDto>> getMagicBagsByCategory(@PathVariable String category) {
        List<MagicBagDto> magicBags = magicBagService.getMagicBagsByCategory(category);
        return Result.success(magicBags);
    }
    
    /**
     * 根据商家ID获取盲盒
     */
    @GetMapping("/merchant/{merchantId}")
    public Result<List<MagicBagDto>> getMagicBagsByMerchantId(@PathVariable Integer merchantId) {
        List<MagicBagDto> magicBags = magicBagService.getMagicBagsByMerchantId(merchantId);
        return Result.success(magicBags);
    }
}
