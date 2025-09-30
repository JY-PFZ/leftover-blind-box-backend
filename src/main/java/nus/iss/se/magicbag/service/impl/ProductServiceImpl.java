package nus.iss.se.magicbag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import nus.iss.se.magicbag.dto.ProductDto;
import nus.iss.se.magicbag.dto.ProductListResponse;
import nus.iss.se.magicbag.entity.Product;
import nus.iss.se.magicbag.mapper.ProductMapper;
import nus.iss.se.magicbag.service.IProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements IProductService {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Override
    public ProductListResponse getAllProducts(Integer page, Integer size) {
        Page<Product> productPage = new Page<>(page, size);
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_active", true);
        
        Page<Product> result = productMapper.selectPage(productPage, queryWrapper);
        
        List<ProductDto> productDtos = result.getRecords().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        ProductListResponse response = new ProductListResponse();
        response.setProducts(productDtos);
        response.setTotalItems(result.getTotal());
        response.setCurrentPage((int) result.getCurrent());
        response.setPageSize((int) result.getSize());
        response.setTotalPages((int) result.getPages());
        
        return response;
    }
    
    @Override
    public ProductDto getProductById(Integer id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return null;
        }
        return convertToDto(product);
    }
    
    @Override
    public List<ProductDto> getProductsByCategory(String category) {
        List<Product> products = productMapper.findByCategory(category);
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProductDto> getProductsByMerchantId(Integer merchantId) {
        List<Product> products = productMapper.findByMerchantId(merchantId);
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        BeanUtils.copyProperties(product, dto);
        return dto;
    }
}
