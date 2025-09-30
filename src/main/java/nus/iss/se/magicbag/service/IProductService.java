package nus.iss.se.magicbag.service;

import nus.iss.se.magicbag.dto.ProductDto;
import nus.iss.se.magicbag.dto.ProductListResponse;

import java.util.List;

public interface IProductService {
    
    /**
     * 获取所有商品列表（分页）
     */
    ProductListResponse getAllProducts(Integer page, Integer size);
    
    /**
     * 根据ID获取商品详情
     */
    ProductDto getProductById(Integer id);
    
    /**
     * 根据分类获取商品
     */
    List<ProductDto> getProductsByCategory(String category);
    
    /**
     * 根据商家ID获取商品
     */
    List<ProductDto> getProductsByMerchantId(Integer merchantId);
}


