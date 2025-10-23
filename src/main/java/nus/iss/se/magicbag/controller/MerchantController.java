package nus.iss.se.magicbag.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.auth.common.UserContext; // 🟢 确保导入
import nus.iss.se.magicbag.auth.common.UserContextHolder; // 🟢 确保导入
import nus.iss.se.magicbag.common.Result;
import nus.iss.se.magicbag.common.constant.ResultStatus; // 🟢 确保导入
import nus.iss.se.magicbag.common.exception.BusinessException; // 🟢 确保导入
import nus.iss.se.magicbag.dto.MerchantDto;
import nus.iss.se.magicbag.dto.MerchantLocationDto;
import nus.iss.se.magicbag.dto.MerchantUpdateDto;
import nus.iss.se.magicbag.service.IMerchantService;
import nus.iss.se.magicbag.service.MerchantLocationService;
import org.springframework.security.access.prepost.PreAuthorize; // 🟢 确保导入
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchants")
@CrossOrigin(origins = "*") // 注意：生产环境中通常不建议使用 "*"
@RequiredArgsConstructor
@Tag(name = "Merchant API", description = "商家管理服务")
public class MerchantController {

    private final IMerchantService merchantService;
    private final UserContextHolder userContextHolder;
    private final MerchantLocationService merchantLocationService;

    /**
     * 获取所有已审核的商家列表 (对所有用户开放)
     */
    @GetMapping
    @Operation(summary = "获取所有已审核商家列表", description = "获取所有状态为 'approved' 的商家")
    public Result<List<MerchantDto>> getAllApprovedMerchants() {
        // 注意：原 getAllMerchants 可能需要调整为只返回 approved 状态
        List<MerchantDto> merchants = merchantService.getAllApprovedMerchants(); // 假设服务层有此方法
        return Result.success(merchants);
    }

    /**
     * 🟢 新增：获取当前登录商家的信息 (仅限商家访问)
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('MERCHANT')") // 保护此接口，只有商家能访问
    @Operation(summary = "获取当前商家信息", description = "获取当前登录用户的商家详细信息")
    public Result<MerchantDto> getMyMerchantProfile() {
        UserContext currentUser = userContextHolder.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            // 这通常不应该发生，因为 @PreAuthorize 已经验证过
            throw new BusinessException(ResultStatus.ACCESS_DENIED, "User context not found.");
        }
        Integer userId = currentUser.getId();
        MerchantDto merchant = merchantService.findByUserId(userId); // 🟢 调用服务层新方法
        if (merchant == null) {
            // 这里返回具体的错误，而不是通用的 User Not Found
            return Result.error(ResultStatus.MERCHANT_NOT_FOUND.getCode(), "No merchant profile associated with the current user.");
        }
        return Result.success(merchant);
    }

    /**
     * 根据ID获取商家详情 (对所有用户开放)
     * 🔴 注意：这个路径必须在 "/my" 之后定义，以避免路由冲突
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取商家详情", description = "根据商家ID获取详细信息")
    public Result<MerchantDto> getMerchantById(@PathVariable Integer id) {
        MerchantDto merchant = merchantService.getMerchantById(id);
        if (merchant == null) {
            return Result.error(ResultStatus.MERCHANT_NOT_FOUND.getCode(), "Merchant not found."); // 使用标准状态码
        }
        return Result.success(merchant);
    }

    /**
     * 更新商家自己的信息 (仅限商家访问)
     */
    @PutMapping("/profile") // 或者可以改为 @PutMapping("/my/profile") 保持一致性
    @PreAuthorize("hasRole('MERCHANT')") // 保护此接口
    @Operation(summary = "更新商家信息", description = "商家更新自己的店铺信息")
    public Result<Void> updateMyMerchantProfile(@RequestBody @Valid MerchantUpdateDto merchantDto) {
        UserContext currentUser = userContextHolder.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED, "User context not found.");
        }
        // 调用服务层方法，传入 DTO 和当前用户信息
        merchantService.updateMerchantProfile(merchantDto, currentUser);
        return Result.success();
    }

    /**
     * 查询周边商家 (对所有用户开放)
     */
    @GetMapping("/nearby")
    @Operation(summary = "查询周边商家，根据距离排序", description = "根据经纬度查询周边商家")
    public Result<List<MerchantLocationDto>> getNearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "1") double radius) { // radius 单位通常是 km

        // 注意：lon 和 lat 的顺序可能需要根据 service 实现调整
        List<MerchantLocationDto> nearbyMerchants = merchantLocationService.getNearbyMerchants(lon, lat, radius);
        return Result.success(nearbyMerchants);
    }

    /**
     * 根据评分排序商铺 (对所有用户开放)
     */
    @GetMapping("/sorted-by-score")
    @Operation(summary = "根据评分排序商铺", description = "分页获取根据评分排序的商铺列表")
    public Result<IPage<MerchantDto>> sortedByScore(
            @RequestParam(defaultValue = "1", name = "current") Integer current,
            @RequestParam(defaultValue = "10", name = "size") Integer size,
            @RequestParam(defaultValue = "0", name = "minScore") Integer minScore){ // minScore 通常是浮点数，如 0.0 或 3.5
        // 确保页码和大小是有效的
        current = Math.max(1, current);
        size = Math.max(1, Math.min(100, size)); // 限制每页大小

        IPage<MerchantDto> listByScore = merchantService.sortedMerchantsByScore(current, size, minScore);
        return Result.success(listByScore);
    }
}
