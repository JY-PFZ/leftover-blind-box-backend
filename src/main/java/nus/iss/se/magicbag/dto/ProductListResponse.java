package nus.iss.se.magicbag.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductListResponse {
    private List<ProductDto> products;
    private Long totalItems;
    private Integer currentPage;
    private Integer pageSize;
    private Integer totalPages;
}


