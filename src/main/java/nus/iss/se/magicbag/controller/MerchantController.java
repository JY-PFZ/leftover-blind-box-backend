package nus.iss.se.magicbag.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.auth.common.UserContextHolder;
import nus.iss.se.magicbag.common.Result;
import nus.iss.se.magicbag.dto.MerchantDto;
import nus.iss.se.magicbag.dto.MerchantUpdateDto;
import nus.iss.se.magicbag.service.IMerchantService;
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
}


