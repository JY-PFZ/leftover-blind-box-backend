package nus.iss.se.magicbag.service.impl;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.auth.common.UserContextHolder;
import nus.iss.se.magicbag.common.constant.ResultStatus;
import nus.iss.se.magicbag.common.exception.BusinessException;
import nus.iss.se.magicbag.dto.MerchantDto;
import nus.iss.se.magicbag.dto.MerchantRegisterDto;
import nus.iss.se.magicbag.entity.Merchant;
import nus.iss.se.magicbag.mapper.MerchantMapper;
import nus.iss.se.magicbag.mapper.UserMapper;
import nus.iss.se.magicbag.auth.service.UserCacheService;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;

class MerchantServiceImplTest {

    @InjectMocks
    private MerchantServiceImpl merchantService;

    @Mock
    private MerchantMapper merchantMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private UserContextHolder userContextHolder;
    @Mock
    private UserCacheService userCacheService;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        Field baseMapperField = merchantService.getClass().getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(merchantService, merchantMapper);
    }

    @Test
    void testRegisterMerchant_UserNotLoggedIn() {
        MerchantRegisterDto dto = new MerchantRegisterDto();
        dto.setName("测试商家");
        dto.setAddress("测试地址");

        when(userContextHolder.getCurrentUser()).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> merchantService.registerMerchant(dto));
        
        assertEquals(ResultStatus.USER_NOT_LOGGED_IN, exception.getErrInfo());
    }

    @Test
    void testRegisterMerchant_Success() {
        MerchantRegisterDto dto = new MerchantRegisterDto();
        dto.setName("测试商家");
        dto.setPhone("81234567");
        dto.setAddress("测试地址");

        UserContext userContext = new UserContext();
        userContext.setId(1);
        when(userContextHolder.getCurrentUser()).thenReturn(userContext);
        when(merchantMapper.findByUserId(1)).thenReturn(null);
        when(merchantMapper.findByPhone("81234567")).thenReturn(null);
        when(merchantMapper.insert(any(Merchant.class))).thenAnswer(invocation -> {
            Merchant m = invocation.getArgument(0);
            m.setId(1);
            return 1;
        });

        assertDoesNotThrow(() -> merchantService.registerMerchant(dto));
        verify(merchantMapper, times(1)).insert(any(Merchant.class));
    }

    @Test
    void testGetMerchantById_Success() {
        Merchant merchant = new Merchant();
        merchant.setId(1);
        merchant.setName("测试商家");
        when(merchantMapper.selectById(1)).thenReturn(merchant);

        MerchantDto result = merchantService.getMerchantById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("测试商家", result.getName());
    }

    @Test
    void testGetMerchantById_NotFound() {
        when(merchantMapper.selectById(999)).thenReturn(null);
        MerchantDto result = merchantService.getMerchantById(999);
        assertNull(result);
    }
}
