package nus.iss.se.magicbag.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.auth.common.UserContextHolder;
import nus.iss.se.magicbag.common.Result;
import nus.iss.se.magicbag.dto.MerchantDto;
import nus.iss.se.magicbag.dto.MerchantLocationDto;
import nus.iss.se.magicbag.dto.MerchantUpdateDto;
import nus.iss.se.magicbag.service.IMerchantService;
import nus.iss.se.magicbag.service.MerchantLocationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchants")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Merchant API", description = "商家管理服务")
public class MerchantController {
    
    private final IMerchantService merchantService;
    private final UserContextHolder userContextHolder;
    private final MerchantLocationService merchantLocationService;
    
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
    @Operation(summary = "根据ID获取商家详情", description = "根据商家ID获取详细信息")
    public Result<MerchantDto> getMerchantById(@PathVariable Integer id) {
        MerchantDto merchant = merchantService.getMerchantById(id);
        if (merchant == null) {
            return Result.error("商家不存在");
        }
        return Result.success(merchant);
    }

    @PostMapping("/register")
    @Operation(summary = "注册商家信息", description = "用户注册自己的店铺信息")
    public Result<Void> registerMerchantProfile(@RequestBody @Valid MerchantUpdateDto merchantDto) {
        merchantService.registerMerchant(merchantDto);
        return Result.success();
    }
    
    /**
     * 更新商家信息
     */
    @PutMapping("/profile")
    @Operation(summary = "更新商家信息", description = "商家更新自己的店铺信息")
    public Result<Void> updateMerchantProfile(@RequestBody @Valid MerchantUpdateDto merchantDto) {
        UserContext currentUser = userContextHolder.getCurrentUser();
        merchantService.updateMerchantProfile(merchantDto, currentUser);
        return Result.success();
    }

    @GetMapping("/nearby")
    @Operation(summary = "查询周边商家，根据距离排序", description = "根据经纬度查询周边商家")
    public Result<List<MerchantLocationDto>> getNearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "1") double radius) {

        List<MerchantLocationDto> nearbyMerchants = merchantLocationService.getNearbyMerchants(lon, lat, radius);
        return Result.success(nearbyMerchants);
    }

    @GetMapping("/sorted-by-score")
    @Operation(summary = "根据评分排序商铺", description = "根据评分排序商铺")
    public Result<IPage<MerchantDto>> sortedByScore(@RequestParam(defaultValue = "1", name = "current") Integer current, @RequestParam(defaultValue = "10", name = "size") Integer size, @RequestParam(defaultValue = "0", name = "minScore")Integer minScore){
        IPage<MerchantDto> listByScore = merchantService.sortedMerchantsByScore(current,size, minScore);
        return Result.success(listByScore);
    }
}


