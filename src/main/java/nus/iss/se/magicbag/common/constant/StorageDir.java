package nus.iss.se.magicbag.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StorageDir {
    PRODUCT_IMAGES_DIR("product_images","存商品图片");

    private final String code;
    private final String desc;
}
