package nus.iss.se.magicbag.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import nus.iss.se.magicbag.common.Result;
import nus.iss.se.magicbag.common.constant.ResultStatus;
import nus.iss.se.magicbag.dto.MagicBagCreateDto;
import nus.iss.se.magicbag.dto.MagicBagDto;
import nus.iss.se.magicbag.service.FileService;
import nus.iss.se.magicbag.service.IMagicBagService;

import java.time.LocalTime;
import java.util.Date;

class MagicBagControllerTest {

    @InjectMocks
    private MagicBagController magicBagController;

    @Mock
    private IMagicBagService magicBagService;
    @Mock
    private FileService fileService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetMagicBagById_Success() {
        MagicBagDto bag = new MagicBagDto();
        bag.setId(1);
        bag.setTitle("测试盲盒");
        when(magicBagService.getMagicBagById(1)).thenReturn(bag);

        Result<MagicBagDto> result = magicBagController.getMagicBagById(1);

        assertNotNull(result);
        assertEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getId());
    }

    @Test
    void testGetMagicBagById_NotFound() {
        when(magicBagService.getMagicBagById(999)).thenReturn(null);

        Result<MagicBagDto> result = magicBagController.getMagicBagById(999);

        assertNotNull(result);
        assertNotEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
    }

    @Test
    void testCreateMagicBag_Success() {
        MagicBagCreateDto dto = new MagicBagCreateDto();
        dto.setMerchantId(1);
        dto.setTitle("新盲盒");
        dto.setPrice(25.0f);
        dto.setQuantity(10);
        dto.setPickupStartTime(LocalTime.of(9, 0));
        dto.setPickupEndTime(LocalTime.of(18, 0));
        dto.setAvailableDate(new Date());

        MagicBagDto created = new MagicBagDto();
        created.setId(1);
        when(magicBagService.createMagicBag(any())).thenReturn(created);

        Result<MagicBagDto> result = magicBagController.createMagicBag(dto);

        assertNotNull(result);
        assertEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
        verify(magicBagService, times(1)).createMagicBag(any());
    }

    @Test
    void testDeleteMagicBag_Success() {
        when(magicBagService.deleteMagicBag(1)).thenReturn(true);

        Result<Void> result = magicBagController.deleteMagicBag(1);

        assertNotNull(result);
        assertEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
        verify(magicBagService, times(1)).deleteMagicBag(1);
    }
}
