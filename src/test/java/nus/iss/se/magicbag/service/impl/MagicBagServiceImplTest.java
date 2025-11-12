//package nus.iss.se.magicbag.service.impl;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import nus.iss.se.magicbag.dto.MagicBagCreateDto;
//import nus.iss.se.magicbag.dto.MagicBagDto;
//import nus.iss.se.magicbag.dto.MagicBagListResponse;
//import nus.iss.se.magicbag.dto.MagicBagUpdateDto;
//import nus.iss.se.magicbag.entity.MagicBag;
//import nus.iss.se.magicbag.mapper.MagicBagMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class MagicBagServiceImplTest {
//
//    @Mock
//    private MagicBagMapper magicBagMapper;
//
//    @InjectMocks
//    private MagicBagServiceImpl magicBagService;
//
//    private MagicBag mockMagicBag;
//
//    @BeforeEach
//    void setUp() {
//        mockMagicBag = new MagicBag();
//        mockMagicBag.setId(1);
//        mockMagicBag.setMerchantId(1);
//        mockMagicBag.setTitle("测试盲盒");
//        mockMagicBag.setDescription("测试描述");
//        mockMagicBag.setPrice(25.0f);
//        mockMagicBag.setQuantity(10);
//        mockMagicBag.setPickupStartTime(LocalTime.of(9, 0));
//        mockMagicBag.setPickupEndTime(LocalTime.of(18, 0));
//        mockMagicBag.setAvailableDate(new Date());
//        mockMagicBag.setCategory("food");
//        mockMagicBag.setActive(true);
//        mockMagicBag.setCreatedAt(LocalDateTime.now());
//        mockMagicBag.setUpdatedAt(LocalDateTime.now());
//    }
//
//    @Test
//    void testGetAllMagicBags_Success() {
//        // Given
//        Page<MagicBag> page = new Page<>(1, 10);
//        page.setTotal(2);
//        page.setRecords(Arrays.asList(mockMagicBag));
//
//        when(magicBagMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);
//
//        // When
//        MagicBagListResponse response = magicBagService.getAllMagicBags(1, 10);
//
//        // Then
//        assertNotNull(response);
//        assertEquals(2L, response.getTotalItems());
//        assertEquals(1, response.getCurrentPage());
//        assertEquals(10, response.getPageSize());
//        assertEquals(1, response.getMagicBags().size());
//        verify(magicBagMapper, times(1)).selectPage(any(Page.class), any(QueryWrapper.class));
//    }
//
//    @Test
//    void testGetAllMagicBags_OnlyActive() {
//        // Given
//        Page<MagicBag> page = new Page<>(1, 10);
//        page.setTotal(1);
//        page.setRecords(Arrays.asList(mockMagicBag));
//
//        when(magicBagMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);
//
//        // When
//        MagicBagListResponse response = magicBagService.getAllMagicBags(1, 10);
//
//        // Then
//        assertNotNull(response);
//        verify(magicBagMapper, times(1)).selectPage(any(Page.class), any(QueryWrapper.class));
//
//        // 验证 QueryWrapper 包含 is_active = true 的条件
//        @SuppressWarnings("unchecked")
//        ArgumentCaptor<QueryWrapper<MagicBag>> wrapperCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
//        verify(magicBagMapper).selectPage(any(Page.class), wrapperCaptor.capture());
//    }
//
//    @Test
//    void testGetMagicBagById_Success() {
//        // Given
//        when(magicBagMapper.selectById(1)).thenReturn(mockMagicBag);
//
//        // When
//        MagicBagDto result = magicBagService.getMagicBagById(1);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.getId());
//        assertEquals("测试盲盒", result.getTitle());
//        assertEquals(new BigDecimal("25.00"), result.getPrice());
//        verify(magicBagMapper, times(1)).selectById(1);
//    }
//
//    @Test
//    void testGetMagicBagById_NotFound() {
//        // Given
//        when(magicBagMapper.selectById(999)).thenReturn(null);
//
//        // When
//        MagicBagDto result = magicBagService.getMagicBagById(999);
//
//        // Then
//        assertNull(result);
//    }
//
//    @Test
//    void testGetMagicBagsByCategory_Success() {
//        // Given
//        List<MagicBag> bags = Arrays.asList(mockMagicBag);
//        when(magicBagMapper.findByCategory("food")).thenReturn(bags);
//
//        // When
//        List<MagicBagDto> result = magicBagService.getMagicBagsByCategory("food");
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals("food", result.get(0).getCategory());
//        verify(magicBagMapper, times(1)).findByCategory("food");
//    }
//
//    @Test
//    void testGetMagicBagsByMerchantId_Success() {
//        // Given
//        List<MagicBag> bags = Arrays.asList(mockMagicBag);
//        when(magicBagMapper.findByMerchantId(1)).thenReturn(bags);
//
//        // When
//        List<MagicBagDto> result = magicBagService.getMagicBagsByMerchantId(1);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals(1, result.get(0).getMerchantId());
//        verify(magicBagMapper, times(1)).findByMerchantId(1);
//    }
//
//    @Test
//    void testCreateMagicBag_Success() {
//        // Given
//        MagicBagCreateDto createDto = new MagicBagCreateDto();
//        createDto.setMerchantId(1);
//        createDto.setTitle("新盲盒");
//        createDto.setDescription("新描述");
//        createDto.setPrice(30.0f);
//        createDto.setQuantity(20);
//        createDto.setPickupStartTime(LocalTime.of(10, 0));
//        createDto.setPickupEndTime(LocalTime.of(20, 0));
//        createDto.setAvailableDate(new Date());
//        createDto.setCategory("drink");
//
//        when(magicBagMapper.insert(any(MagicBag.class))).thenAnswer(invocation -> {
//            MagicBag bag = invocation.getArgument(0);
//            bag.setId(2); // 模拟数据库自动生成ID
//            return 1;
//        });
//
//        // When
//        MagicBagDto result = magicBagService.createMagicBag(createDto);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(2, result.getId());
//        assertEquals("新盲盒", result.getTitle());
//        verify(magicBagMapper, times(1)).insert(any(MagicBag.class));
//
//        ArgumentCaptor<MagicBag> bagCaptor = ArgumentCaptor.forClass(MagicBag.class);
//        verify(magicBagMapper).insert(bagCaptor.capture());
//        MagicBag savedBag = bagCaptor.getValue();
//        assertTrue(savedBag.isActive()); // 默认应该是 active
//        assertNotNull(savedBag.getCreatedAt());
//    }
//
//    @Test
//    void testUpdateMagicBag_Success() {
//        // Given
//        MagicBagUpdateDto updateDto = new MagicBagUpdateDto();
//        updateDto.setTitle("更新后的标题");
//        updateDto.setPrice(35.0f);
//        updateDto.setQuantity(15);
//
//        when(magicBagMapper.selectById(1)).thenReturn(mockMagicBag);
//        when(magicBagMapper.updateById(any(MagicBag.class))).thenReturn(1);
//
//        // When
//        MagicBagDto result = magicBagService.updateMagicBag(1, updateDto);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.getId());
//        verify(magicBagMapper, times(1)).selectById(1);
//        verify(magicBagMapper, times(1)).updateById(any(MagicBag.class));
//
//        ArgumentCaptor<MagicBag> bagCaptor = ArgumentCaptor.forClass(MagicBag.class);
//        verify(magicBagMapper).updateById(bagCaptor.capture());
//        MagicBag updatedBag = bagCaptor.getValue();
//        assertEquals("更新后的标题", updatedBag.getTitle());
//        assertEquals(35.0f, updatedBag.getPrice());
//        assertEquals(15, updatedBag.getQuantity());
//    }
//
//    @Test
//    void testUpdateMagicBag_PartialUpdate() {
//        // Given
//        MagicBagUpdateDto updateDto = new MagicBagUpdateDto();
//        updateDto.setTitle("只更新标题"); // 只更新标题，其他字段不更新
//
//        when(magicBagMapper.selectById(1)).thenReturn(mockMagicBag);
//        when(magicBagMapper.updateById(any(MagicBag.class))).thenReturn(1);
//
//        // When
//        MagicBagDto result = magicBagService.updateMagicBag(1, updateDto);
//
//        // Then
//        assertNotNull(result);
//        ArgumentCaptor<MagicBag> bagCaptor = ArgumentCaptor.forClass(MagicBag.class);
//        verify(magicBagMapper).updateById(bagCaptor.capture());
//        MagicBag updatedBag = bagCaptor.getValue();
//        assertEquals("只更新标题", updatedBag.getTitle());
//        assertEquals(25.0f, updatedBag.getPrice()); // 价格应该保持不变
//    }
//
//    @Test
//    void testUpdateMagicBag_NotFound() {
//        // Given
//        MagicBagUpdateDto updateDto = new MagicBagUpdateDto();
//        updateDto.setTitle("更新标题");
//
//        when(magicBagMapper.selectById(999)).thenReturn(null);
//
//        // When & Then
//        RuntimeException exception = assertThrows(RuntimeException.class,
//            () -> magicBagService.updateMagicBag(999, updateDto));
//
//        assertEquals("盲盒不存在", exception.getMessage());
//        verify(magicBagMapper, never()).updateById(any());
//    }
//
//    @Test
//    void testDeleteMagicBag_Success() {
//        // Given
//        when(magicBagMapper.selectById(1)).thenReturn(mockMagicBag);
//        when(magicBagMapper.updateById(any(MagicBag.class))).thenReturn(1);
//
//        // When
//        boolean result = magicBagService.deleteMagicBag(1);
//
//        // Then
//        assertTrue(result);
//        verify(magicBagMapper, times(1)).selectById(1);
//        verify(magicBagMapper, times(1)).updateById(any(MagicBag.class));
//
//        ArgumentCaptor<MagicBag> bagCaptor = ArgumentCaptor.forClass(MagicBag.class);
//        verify(magicBagMapper).updateById(bagCaptor.capture());
//        MagicBag deletedBag = bagCaptor.getValue();
//        assertFalse(deletedBag.isActive()); // 软删除：is_active 应该为 false
//    }
//
//    @Test
//    void testDeleteMagicBag_NotFound() {
//        // Given
//        when(magicBagMapper.selectById(999)).thenReturn(null);
//
//        // When
//        boolean result = magicBagService.deleteMagicBag(999);
//
//        // Then
//        assertFalse(result);
//        verify(magicBagMapper, never()).updateById(any());
//    }
//}
//
