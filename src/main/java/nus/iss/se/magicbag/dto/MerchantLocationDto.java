package nus.iss.se.magicbag.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MerchantLocationDto {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private double distance;
    private String unit;
}
