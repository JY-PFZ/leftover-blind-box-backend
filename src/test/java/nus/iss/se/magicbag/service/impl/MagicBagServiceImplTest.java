package nus.iss.se.magicbag.service.impl;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import nus.iss.se.magicbag.dto.MagicBagCreateDto;
import nus.iss.se.magicbag.dto.MagicBagDto;
import nus.iss.se.magicbag.entity.MagicBag;
import nus.iss.se.magicbag.mapper.MagicBagMapper;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.Date;

class MagicBagServiceImplTest {

    @InjectMocks
    private MagicBagServiceImpl magicBagService;

    @Mock
    private MagicBagMapper magicBagMapper;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        Field baseMapperField = magicBagService.getClass().getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(magicBagService, magicBagMapper);
    }

    @Test
    void testGetMagicBagById_Success() {
        MagicBag bag = new MagicBag();
        bag.setId(1);
        bag.setTitle("测试盲盒");
        bag.setPrice(25.0f);
        bag.setActive(true);
        when(magicBagMapper.selectById(1)).thenReturn(bag);

        MagicBagDto result = magicBagService.getMagicBagById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("测试盲盒", result.getTitle());
    }

    @Test
    void testGetMagicBagById_NotFound() {
        when(magicBagMapper.selectById(999)).thenReturn(null);
        MagicBagDto result = magicBagService.getMagicBagById(999);
        assertNull(result);
    }

    @Test
    void testCreateMagicBag_Success() {
        MagicBagCreateDto dto = new MagicBagCreateDto();
        dto.setMerchantId(1);
        dto.setTitle("新盲盒");
        dto.setPrice(30.0f);
        dto.setQuantity(10);
        dto.setPickupStartTime(LocalTime.of(9, 0));
        dto.setPickupEndTime(LocalTime.of(18, 0));
        dto.setAvailableDate(new Date());

        when(magicBagMapper.insert(any(MagicBag.class))).thenAnswer(invocation -> {
            MagicBag bag = invocation.getArgument(0);
            bag.setId(1);
            return 1;
        });

        MagicBagDto result = magicBagService.createMagicBag(dto);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(magicBagMapper, times(1)).insert(any(MagicBag.class));
    }

    @Test
    void testDeleteMagicBag_Success() {
        MagicBag bag = new MagicBag();
        bag.setId(1);
        bag.setActive(true);
        when(magicBagMapper.selectById(1)).thenReturn(bag);
        when(magicBagMapper.updateById(any(MagicBag.class))).thenReturn(1);

        boolean result = magicBagService.deleteMagicBag(1);

        assertTrue(result);
        verify(magicBagMapper, times(1)).updateById(any(MagicBag.class));
    }
}
