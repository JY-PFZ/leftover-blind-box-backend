package nus.iss.se.magicbag.dto;

import lombok.Data;
import java.util.List;

@Data
public class MagicBagListResponse {
    private List<MagicBagDto> magicBags;
    private Long totalItems;
    private Integer currentPage;
    private Integer pageSize;
    private Integer totalPages;
}
