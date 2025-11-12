//package nus.iss.se.magicbag.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import nus.iss.se.magicbag.common.constant.ResultStatus;
//import nus.iss.se.magicbag.dto.MagicBagCreateDto;
//import nus.iss.se.magicbag.dto.MagicBagDto;
//import nus.iss.se.magicbag.dto.MagicBagListResponse;
//import nus.iss.se.magicbag.dto.MagicBagUpdateDto;
//import nus.iss.se.magicbag.service.FileService;
//import nus.iss.se.magicbag.service.IMagicBagService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.math.BigDecimal;
//import java.time.LocalTime;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(MagicBagController.class)
//class MagicBagControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private IMagicBagService magicBagService;
//
//    @MockitoBean
//    private FileService fileService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    void testGetAllMagicBags_Success() throws Exception {
//        // Given
//        MagicBagListResponse response = new MagicBagListResponse();
//        response.setTotalItems(2L);
//        response.setCurrentPage(1);
//        response.setPageSize(10);
//        response.setTotalPages(1);
//
//        MagicBagDto bag1 = new MagicBagDto();
//        bag1.setId(1);
//        bag1.setTitle("盲盒1");
//        bag1.setPrice(new BigDecimal("10.50"));
//
//        MagicBagDto bag2 = new MagicBagDto();
//        bag2.setId(2);
//        bag2.setTitle("盲盒2");
//        bag2.setPrice(new BigDecimal("20.00"));
//
//        response.setMagicBags(Arrays.asList(bag1, bag2));
//
//        when(magicBagService.getAllMagicBags(1, 10)).thenReturn(response);
//
//        // When & Then
//        mockMvc.perform(get("/api/product")
//                        .param("page", "1")
//                        .param("size", "10"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(ResultStatus.SUCCESS.getCode()))
//                .andExpect(jsonPath("$.data.totalItems").value(2))
//                .andExpect(jsonPath("$.data.magicBags").isArray());
//
//        verify(magicBagService, times(1)).getAllMagicBags(1, 10);
//    }
//
//    @Test
//    void testGetMagicBagById_Success() throws Exception {
//        // Given
//        MagicBagDto bag = new MagicBagDto();
//        bag.setId(1);
//        bag.setTitle("测试盲盒");
//        bag.setPrice(new BigDecimal("15.00"));
//        bag.setIsActive(true);
//
//        when(magicBagService.getMagicBagById(1)).thenReturn(bag);
//
//        // When & Then
//        mockMvc.perform(get("/api/product/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(ResultStatus.SUCCESS.getCode()))
//                .andExpect(jsonPath("$.data.id").value(1))
//                .andExpect(jsonPath("$.data.title").value("测试盲盒"));
//
//        verify(magicBagService, times(1)).getMagicBagById(1);
//    }
//
//    @Test
//    void testGetMagicBagById_NotFound() throws Exception {
//        // Given
//        when(magicBagService.getMagicBagById(999)).thenReturn(null);
//
//        // When & Then
//        mockMvc.perform(get("/api/product/999"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(ResultStatus.FAIL.getCode()));
//
//        verify(magicBagService, times(1)).getMagicBagById(999);
//    }
//
//    @Test
//    void testGetMagicBagsByCategory_Success() throws Exception {
//        // Given
//        MagicBagDto bag = new MagicBagDto();
//        bag.setId(1);
//        bag.setTitle("食品盲盒");
//        bag.setCategory("food");
//
//        List<MagicBagDto> bags = Arrays.asList(bag);
//        when(magicBagService.getMagicBagsByCategory("food")).thenReturn(bags);
//
//        // When & Then
//        mockMvc.perform(get("/api/product/category/food"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(ResultStatus.SUCCESS.getCode()))
//                .andExpect(jsonPath("$.data").isArray())
//                .andExpect(jsonPath("$.data.length()").value(1));
//
//        verify(magicBagService, times(1)).getMagicBagsByCategory("food");
//    }
//
//    @Test
//    void testGetMagicBagsByMerchantId_Success() throws Exception {
//        // Given
//        MagicBagDto bag = new MagicBagDto();
//        bag.setId(1);
//        bag.setMerchantId(1);
//        bag.setTitle("商家1的盲盒");
//
//        List<MagicBagDto> bags = Arrays.asList(bag);
//        when(magicBagService.getMagicBagsByMerchantId(1)).thenReturn(bags);
//
//        // When & Then
//        mockMvc.perform(get("/api/product/merchant/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(ResultStatus.SUCCESS.getCode()))
//                .andExpect(jsonPath("$.data").isArray());
//
//        verify(magicBagService, times(1)).getMagicBagsByMerchantId(1);
//    }
//
//    @Test
//    void testCreateMagicBag_Success() throws Exception {
//        // Given
//        MagicBagCreateDto createDto = new MagicBagCreateDto();
//        createDto.setMerchantId(1);
//        createDto.setTitle("新盲盒");
//        createDto.setPrice(25.0f);
//        createDto.setQuantity(10);
//        createDto.setPickupStartTime(LocalTime.of(9, 0));
//        createDto.setPickupEndTime(LocalTime.of(18, 0));
//        createDto.setAvailableDate(new Date());
//        createDto.setCategory("food");
//
//        MagicBagDto createdBag = new MagicBagDto();
//        createdBag.setId(1);
//        createdBag.setTitle("新盲盒");
//        createdBag.setPrice(new BigDecimal("25.00"));
//
//        when(magicBagService.createMagicBag(any(MagicBagCreateDto.class))).thenReturn(createdBag);
//
//        // When & Then
//        mockMvc.perform(post("/api/product")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(createDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(ResultStatus.SUCCESS.getCode()))
//                .andExpect(jsonPath("$.data.id").value(1));
//
//        verify(magicBagService, times(1)).createMagicBag(any(MagicBagCreateDto.class));
//    }
//
//    @Test
//    void testCreateMagicBag_ValidationFailed_PriceNegative() throws Exception {
//        // Given
//        MagicBagCreateDto createDto = new MagicBagCreateDto();
//        createDto.setMerchantId(1);
//        createDto.setTitle("新盲盒");
//        createDto.setPrice(-10.0f); // 负数价格，应该验证失败
//        createDto.setQuantity(10);
//        createDto.setPickupStartTime(LocalTime.of(9, 0));
//        createDto.setPickupEndTime(LocalTime.of(18, 0));
//        createDto.setAvailableDate(new Date());
//
//        // When & Then
//        mockMvc.perform(post("/api/product")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(createDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(ResultStatus.FAIL.getCode()));
//
//        verify(magicBagService, never()).createMagicBag(any());
//    }
//
//    @Test
//    void testUpdateMagicBag_Success() throws Exception {
//        // Given
//        MagicBagUpdateDto updateDto = new MagicBagUpdateDto();
//        updateDto.setTitle("更新后的标题");
//        updateDto.setPrice(30.0f);
//
//        MagicBagDto updatedBag = new MagicBagDto();
//        updatedBag.setId(1);
//        updatedBag.setTitle("更新后的标题");
//        updatedBag.setPrice(new BigDecimal("30.00"));
//
//        when(magicBagService.updateMagicBag(eq(1), any(MagicBagUpdateDto.class))).thenReturn(updatedBag);
//
//        // When & Then
//        mockMvc.perform(put("/api/product/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(ResultStatus.SUCCESS.getCode()))
//                .andExpect(jsonPath("$.data.title").value("更新后的标题"));
//
//        verify(magicBagService, times(1)).updateMagicBag(eq(1), any(MagicBagUpdateDto.class));
//    }
//
//    @Test
//    void testUpdateMagicBag_NotFound() throws Exception {
//        // Given
//        MagicBagUpdateDto updateDto = new MagicBagUpdateDto();
//        updateDto.setTitle("更新后的标题");
//
//        when(magicBagService.updateMagicBag(eq(999), any(MagicBagUpdateDto.class)))
//                .thenThrow(new RuntimeException("盲盒不存在"));
//
//        // When & Then
//        mockMvc.perform(put("/api/product/999")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(ResultStatus.FAIL.getCode()));
//
//        verify(magicBagService, times(1)).updateMagicBag(eq(999), any(MagicBagUpdateDto.class));
//    }
//
//    @Test
//    void testDeleteMagicBag_Success() throws Exception {
//        // Given
//        when(magicBagService.deleteMagicBag(1)).thenReturn(true);
//
//        // When & Then
//        mockMvc.perform(delete("/api/product/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(ResultStatus.SUCCESS.getCode()));
//
//        verify(magicBagService, times(1)).deleteMagicBag(1);
//    }
//
//    @Test
//    void testDeleteMagicBag_NotFound() throws Exception {
//        // Given
//        when(magicBagService.deleteMagicBag(999)).thenReturn(false);
//
//        // When & Then
//        mockMvc.perform(delete("/api/product/999"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(ResultStatus.FAIL.getCode()));
//
//        verify(magicBagService, times(1)).deleteMagicBag(999);
//    }
//
//    @Test
//    void testUploadProductImage_Success() throws Exception {
//        // Given
//        MockMultipartFile file = new MockMultipartFile(
//                "file",
//                "test.jpg",
//                MediaType.IMAGE_JPEG_VALUE,
//                "test image content".getBytes()
//        );
//
//        when(fileService.uploadFile(anyString(), anyString(), any())).thenReturn("s3-key-path");
//
//        // When & Then
//        mockMvc.perform(multipart("/api/product/1/image")
//                        .file(file))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(ResultStatus.SUCCESS.getCode()));
//
//        verify(fileService, times(1)).uploadFile(anyString(), anyString(), any());
//    }
//}
//
