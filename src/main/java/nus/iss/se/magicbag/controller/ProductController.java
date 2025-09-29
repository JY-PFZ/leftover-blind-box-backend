package nus.iss.se.magicbag.controller;

import nus.iss.se.magicbag.common.Result;
import nus.iss.se.magicbag.dto.ProductDto;
import nus.iss.se.magicbag.dto.ProductListResponse;
import nus.iss.se.magicbag.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    
    @Autowired
    private IProductService productService;
    
    /**
     * 获取所有商品列表（分页）
     */
    @GetMapping
    public Result<ProductListResponse> getAllProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        ProductListResponse response = productService.getAllProducts(page, size);
        return Result.success(response);
    }
    
    /**
     * 根据ID获取商品详情
     */
    @GetMapping("/{id}")
    public Result<ProductDto> getProductById(@PathVariable Long id) {
        ProductDto product = productService.getProductById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }
    
    /**
     * 根据分类获取商品
     */
    @GetMapping("/category/{category}")
    public Result<List<ProductDto>> getProductsByCategory(@PathVariable String category) {
        List<ProductDto> products = productService.getProductsByCategory(category);
        return Result.success(products);
    }
    
    /**
     * 根据商家ID获取商品
     */
    @GetMapping("/merchant/{merchantId}")
    public Result<List<ProductDto>> getProductsByMerchantId(@PathVariable Long merchantId) {
        List<ProductDto> products = productService.getProductsByMerchantId(merchantId);
        return Result.success(products);
    }
}
