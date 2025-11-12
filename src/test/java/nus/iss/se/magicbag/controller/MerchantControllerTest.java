package nus.iss.se.magicbag.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import nus.iss.se.magicbag.auth.common.UserContextHolder;
import nus.iss.se.magicbag.common.Result;
import nus.iss.se.magicbag.common.constant.ResultStatus;
import nus.iss.se.magicbag.dto.MerchantDto;
import nus.iss.se.magicbag.dto.MerchantRegisterDto;
import nus.iss.se.magicbag.service.IMerchantService;
import nus.iss.se.magicbag.service.MerchantLocationService;

import java.util.Arrays;
import java.util.List;

class MerchantControllerTest {

    @InjectMocks
    private MerchantController merchantController;

    @Mock
    private IMerchantService merchantService;
    @Mock
    private UserContextHolder userContextHolder;
    @Mock
    private MerchantLocationService merchantLocationService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterMerchant_Success() {
        MerchantRegisterDto dto = new MerchantRegisterDto();
        dto.setName("测试商家");
        dto.setPhone("81234567");
        dto.setAddress("测试地址");

        doNothing().when(merchantService).registerMerchant(any());

        Result<Void> result = merchantController.registerMerchantProfile(dto);

        assertNotNull(result);
        assertEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
        verify(merchantService, times(1)).registerMerchant(any());
    }

    @Test
    void testGetMerchantById_Success() {
        MerchantDto merchant = new MerchantDto();
        merchant.setId(1);
        merchant.setName("测试商家");
        when(merchantService.getMerchantById(1)).thenReturn(merchant);

        Result<MerchantDto> result = merchantController.getMerchantById(1);

        assertNotNull(result);
        assertEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getId());
    }

    @Test
    void testGetMerchantById_NotFound() {
        when(merchantService.getMerchantById(999)).thenReturn(null);

        Result<MerchantDto> result = merchantController.getMerchantById(999);

        assertNotNull(result);
        assertNotEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
    }

    @Test
    void testGetAllApprovedMerchants_Success() {
        List<MerchantDto> merchants = Arrays.asList(new MerchantDto());
        when(merchantService.getAllApprovedMerchants()).thenReturn(merchants);

        Result<List<MerchantDto>> result = merchantController.getAllApprovedMerchants();

        assertNotNull(result);
        assertEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
        assertNotNull(result.getData());
    }
}
