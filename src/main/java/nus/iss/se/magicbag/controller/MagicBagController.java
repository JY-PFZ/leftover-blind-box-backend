package nus.iss.se.magicbag.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.common.Result;
import nus.iss.se.magicbag.common.constant.StorageDir;
import nus.iss.se.magicbag.dto.MagicBagCreateDto;
import nus.iss.se.magicbag.dto.MagicBagDto;
import nus.iss.se.magicbag.dto.MagicBagListResponse;
import nus.iss.se.magicbag.dto.MagicBagUpdateDto;
import nus.iss.se.magicbag.entity.MagicBag;
import nus.iss.se.magicbag.service.FileService;
import nus.iss.se.magicbag.service.IMagicBagService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@Tag(name = "MagicBag API", description = "盲盒商品管理服务")
@RequiredArgsConstructor
public class MagicBagController {
    private final IMagicBagService magicBagService;
    private final FileService fileService;
    
    /**
     * 获取所有盲盒列表（分页）
     */
    @GetMapping
    @Operation(summary = "获取所有盲盒列表", description = "分页获取所有已上架的盲盒商品")
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
    @Operation(summary = "根据ID获取盲盒详情", description = "根据盲盒ID获取详细信息")
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
    @Operation(summary = "根据分类获取盲盒", description = "根据分类获取盲盒列表")
    public Result<List<MagicBagDto>> getMagicBagsByCategory(@PathVariable String category) {
        List<MagicBagDto> magicBags = magicBagService.getMagicBagsByCategory(category);
        return Result.success(magicBags);
    }
    
    /**
     * 根据商家ID获取盲盒
     */
    @GetMapping("/merchant/{merchantId}")
    @Operation(summary = "根据商家ID获取盲盒", description = "根据商家ID获取该商家的所有盲盒")
    public Result<List<MagicBagDto>> getMagicBagsByMerchantId(@PathVariable Integer merchantId) {
        List<MagicBagDto> magicBags = magicBagService.getMagicBagsByMerchantId(merchantId);
        return Result.success(magicBags);
    }
    
    /**
     * 创建新的盲盒商品
     */
    @PostMapping
    @Operation(summary = "创建盲盒商品", description = "创建新的盲盒商品")
    public Result<MagicBagDto> createMagicBag(@RequestBody @Valid MagicBagCreateDto createDto) {
        try {
            MagicBagDto magicBag = magicBagService.createMagicBag(createDto);
            return Result.success(magicBag);
        } catch (Exception e) {
            return Result.error("创建盲盒失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新盲盒商品信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新盲盒商品", description = "更新指定ID的盲盒商品信息")
    public Result<MagicBagDto> updateMagicBag(@PathVariable Integer id, 
                                              @RequestBody @Valid MagicBagUpdateDto updateDto) {
        try {
            MagicBagDto magicBag = magicBagService.updateMagicBag(id, updateDto);
            return Result.success(magicBag);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新盲盒失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除盲盒商品
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除盲盒商品", description = "软删除指定ID的盲盒商品（将is_active设置为false）")
    public Result<Void> deleteMagicBag(@PathVariable Integer id) {
        try {
            boolean success = magicBagService.deleteMagicBag(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("盲盒不存在或删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除盲盒失败: " + e.getMessage());
        }
    }

    /**
     * 上传产品图片
     */
    @PostMapping("{id}/image")
    @Operation(summary = "上传产品图片", description = "商户上传盲盒产品图片")
    public Result<String> uploadProductImage(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {

        String fileName = id+"_"+System.currentTimeMillis();
        String key = fileService.uploadFile(StorageDir.PRODUCT_IMAGES_DIR.getCode(), fileName, file);
        LambdaUpdateWrapper<MagicBag> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MagicBag::getId,id).set(MagicBag::getImageUrl,key);
        magicBagService.update(wrapper);

        return Result.success();
    }
}
