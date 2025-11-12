package nus.iss.se.magicbag.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.auth.common.UserContextHolder;
import nus.iss.se.magicbag.common.constant.ResultStatus;
import nus.iss.se.magicbag.dto.MerchantDto;
import nus.iss.se.magicbag.dto.MerchantRegisterDto;
import nus.iss.se.magicbag.dto.MerchantUpdateDto;
import nus.iss.se.magicbag.service.IMerchantService;
import nus.iss.se.magicbag.service.MerchantLocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MerchantController.class)
class MerchantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IMerchantService merchantService;

    @MockitoBean
    private UserContextHolder userContextHolder;

    @MockitoBean
    private MerchantLocationService merchantLocationService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserContext mockUserContext;

    @BeforeEach
    void setUp() {
        mockUserContext = new UserContext();
        mockUserContext.setId(1);
        mockUserContext.setUsername("testuser");
        mockUserContext.setRole("USER");
    }

    @Test
    void testRegisterMerchantProfile_Success() throws Exception {
        // Given
        MerchantRegisterDto dto = new MerchantRegisterDto();
        dto.setName("测试商家");
        dto.setPhone("81234567");
        dto.setAddress("测试地址");
        dto.setLatitude(new BigDecimal("1.3521"));
        dto.setLongitude(new BigDecimal("103.8198"));

        doNothing().when(merchantService).registerMerchant(any(MerchantRegisterDto.class));
        when(userContextHolder.getCurrentUser()).thenReturn(mockUserContext);

        // When & Then
        mockMvc.perform(post("/api/merchant/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultStatus.SUCCESS.getCode()));

        verify(merchantService, times(1)).registerMerchant(any(MerchantRegisterDto.class));
    }

    @Test
    void testRegisterMerchantProfile_ValidationFailed_NameBlank() throws Exception {
        // Given
        MerchantRegisterDto dto = new MerchantRegisterDto();
        dto.setName(""); // 空名称
        dto.setAddress("测试地址");

        // When & Then
        mockMvc.perform(post("/api/merchant/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk()) // Spring 返回 200，但 code 不是 SUCCESS
                .andExpect(jsonPath("$.code").value(ResultStatus.FAIL.getCode()));

        verify(merchantService, never()).registerMerchant(any());
    }

    @Test
    void testRegisterMerchantProfile_ValidationFailed_PhoneFormat() throws Exception {
        // Given
        MerchantRegisterDto dto = new MerchantRegisterDto();
        dto.setName("测试商家");
        dto.setPhone("12345678"); // 错误的手机号格式（不以8或9开头）
        dto.setAddress("测试地址");

        // When & Then
        mockMvc.perform(post("/api/merchant/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultStatus.FAIL.getCode()));

        verify(merchantService, never()).registerMerchant(any());
    }

    @Test
    void testGetAllApprovedMerchants_Success() throws Exception {
        // Given
        MerchantDto merchant1 = new MerchantDto();
        merchant1.setId(1);
        merchant1.setName("商家1");
        merchant1.setStatus("approved");

        MerchantDto merchant2 = new MerchantDto();
        merchant2.setId(2);
        merchant2.setName("商家2");
        merchant2.setStatus("approved");

        List<MerchantDto> merchants = Arrays.asList(merchant1, merchant2);
        when(merchantService.getAllApprovedMerchants()).thenReturn(merchants);

        // When & Then
        mockMvc.perform(get("/api/merchant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultStatus.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(merchantService, times(1)).getAllApprovedMerchants();
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    void testGetMyMerchantProfile_Success() throws Exception {
        // Given
        MerchantDto merchant = new MerchantDto();
        merchant.setId(1);
        merchant.setName("我的商家");
        merchant.setStatus("approved");

        when(userContextHolder.getCurrentUser()).thenReturn(mockUserContext);
        when(merchantService.findByUserId(1)).thenReturn(merchant);

        // When & Then
        mockMvc.perform(get("/api/merchant/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultStatus.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("我的商家"));

        verify(merchantService, times(1)).findByUserId(1);
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    void testGetMyMerchantProfile_NotFound() throws Exception {
        // Given
        when(userContextHolder.getCurrentUser()).thenReturn(mockUserContext);
        when(merchantService.findByUserId(1)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/merchant/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultStatus.MERCHANT_NOT_FOUND.getCode()));

        verify(merchantService, times(1)).findByUserId(1);
    }

    @Test
    void testGetMerchantById_Success() throws Exception {
        // Given
        MerchantDto merchant = new MerchantDto();
        merchant.setId(1);
        merchant.setName("测试商家");
        merchant.setStatus("approved");

        when(merchantService.getMerchantById(1)).thenReturn(merchant);

        // When & Then
        mockMvc.perform(get("/api/merchant/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultStatus.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(merchantService, times(1)).getMerchantById(1);
    }

    @Test
    void testGetMerchantById_NotFound() throws Exception {
        // Given
        when(merchantService.getMerchantById(999)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/merchant/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultStatus.MERCHANT_NOT_FOUND.getCode()));

        verify(merchantService, times(1)).getMerchantById(999);
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    void testUpdateMyMerchantProfile_Success() throws Exception {
        // Given
        MerchantUpdateDto dto = new MerchantUpdateDto();
        dto.setId(1);
        dto.setName("更新后的商家名");
        dto.setPhone("81234567");
        dto.setAddress("新地址");

        doNothing().when(merchantService).updateMerchantProfile(any(MerchantUpdateDto.class), any(UserContext.class));
        when(userContextHolder.getCurrentUser()).thenReturn(mockUserContext);

        // When & Then
        mockMvc.perform(put("/api/merchant/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultStatus.SUCCESS.getCode()));

        verify(merchantService, times(1)).updateMerchantProfile(any(MerchantUpdateDto.class), any(UserContext.class));
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    void testUpdateMyMerchantProfile_ValidationFailed() throws Exception {
        // Given
        MerchantUpdateDto dto = new MerchantUpdateDto();
        // id 为 null，应该验证失败
        dto.setName("测试");

        // When & Then
        mockMvc.perform(put("/api/merchant/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultStatus.FAIL.getCode()));

        verify(merchantService, never()).updateMerchantProfile(any(), any());
    }
}

