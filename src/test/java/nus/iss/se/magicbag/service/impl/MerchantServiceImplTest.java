//package nus.iss.se.magicbag.service.impl;
//
//import nus.iss.se.magicbag.auth.common.UserContext;
//import nus.iss.se.magicbag.auth.common.UserContextHolder;
//import nus.iss.se.magicbag.common.constant.ResultStatus;
//import nus.iss.se.magicbag.common.constant.TaskStatus;
//import nus.iss.se.magicbag.common.exception.BusinessException;
//import nus.iss.se.magicbag.dto.MerchantDto;
//import nus.iss.se.magicbag.dto.MerchantRegisterDto;
//import nus.iss.se.magicbag.dto.MerchantUpdateDto;
//import nus.iss.se.magicbag.dto.event.MerchantProcessedEvent;
//import nus.iss.se.magicbag.entity.Merchant;
//import nus.iss.se.magicbag.entity.User;
//import nus.iss.se.magicbag.mapper.MerchantMapper;
//import nus.iss.se.magicbag.mapper.UserMapper;
//import nus.iss.se.magicbag.auth.service.UserCacheService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.context.ApplicationEventPublisher;
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//
//import java.math.BigDecimal;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class MerchantServiceImplTest {
//
//    @Mock
//    private MerchantMapper merchantMapper;
//
//    @Mock
//    private UserMapper userMapper;
//
//    @Mock
//    private ApplicationEventPublisher eventPublisher;
//
//    @Mock
//    private UserContextHolder userContextHolder;
//
//    @Mock
//    private UserCacheService userCacheService;
//
//    @InjectMocks
//    private MerchantServiceImpl merchantService;
//
//    private UserContext mockUserContext;
//    private Merchant mockMerchant;
//    private User mockUser;
//
//    @BeforeEach
//    void setUp() {
//        mockUserContext = new UserContext();
//        mockUserContext.setId(1);
//        mockUserContext.setUsername("testuser");
//        mockUserContext.setRole("USER");
//
//        mockMerchant = new Merchant();
//        mockMerchant.setId(1);
//        mockMerchant.setUserId(1);
//        mockMerchant.setName("测试商家");
//        mockMerchant.setPhone("81234567");
//        mockMerchant.setAddress("测试地址");
//        mockMerchant.setStatus("pending");
//        mockMerchant.setCreatedAt(new Date());
//        mockMerchant.setUpdatedAt(new Date());
//
//        mockUser = new User();
//        mockUser.setId(1);
//        mockUser.setUsername("testuser");
//        mockUser.setRole("USER");
//    }
//
//    @Test
//    void testRegisterMerchant_Success_NewMerchant() {
//        // Given
//        MerchantRegisterDto dto = new MerchantRegisterDto();
//        dto.setName("新商家");
//        dto.setPhone("81234567");
//        dto.setAddress("新地址");
//        dto.setLatitude(new BigDecimal("1.3521"));
//        dto.setLongitude(new BigDecimal("103.8198"));
//
//        when(userContextHolder.getCurrentUser()).thenReturn(mockUserContext);
//        when(merchantMapper.selectOne(any(QueryWrapper.class))).thenReturn(null); // 没有现有商家
//        when(merchantMapper.findByPhone("81234567")).thenReturn(null); // 手机号未被使用
//        when(merchantMapper.insert(any(Merchant.class))).thenAnswer(invocation -> {
//            Merchant m = invocation.getArgument(0);
//            m.setId(1); // 模拟数据库自动生成ID
//            return 1;
//        });
//        when(merchantMapper.getLastInsertId()).thenReturn(1);
//
//        // When
//        assertDoesNotThrow(() -> merchantService.registerMerchant(dto));
//
//        // Then
//        verify(merchantMapper, times(1)).insert(any(Merchant.class));
//        verify(eventPublisher, times(1)).publishEvent(any());
//
//        ArgumentCaptor<Merchant> merchantCaptor = ArgumentCaptor.forClass(Merchant.class);
//        verify(merchantMapper).insert(merchantCaptor.capture());
//        Merchant savedMerchant = merchantCaptor.getValue();
//        assertEquals("pending", savedMerchant.getStatus());
//        assertEquals(1, savedMerchant.getUserId());
//    }
//
//    @Test
//    void testRegisterMerchant_Failed_UserNotLoggedIn() {
//        // Given
//        MerchantRegisterDto dto = new MerchantRegisterDto();
//        dto.setName("新商家");
//        dto.setAddress("新地址");
//
//        when(userContextHolder.getCurrentUser()).thenReturn(null);
//
//        // When & Then
//        BusinessException exception = assertThrows(BusinessException.class,
//            () -> merchantService.registerMerchant(dto));
//
//        assertEquals(ResultStatus.USER_NOT_LOGGED_IN, exception.getErrInfo());
//        verify(merchantMapper, never()).insert(any());
//    }
//
//    @Test
//    void testRegisterMerchant_Failed_PhoneAlreadyExists() {
//        // Given
//        MerchantRegisterDto dto = new MerchantRegisterDto();
//        dto.setName("新商家");
//        dto.setPhone("81234567");
//        dto.setAddress("新地址");
//
//        Merchant existingMerchant = new Merchant();
//        existingMerchant.setId(2); // 不同的商家ID
//        existingMerchant.setPhone("81234567");
//
//        when(userContextHolder.getCurrentUser()).thenReturn(mockUserContext);
//        when(merchantMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
//        when(merchantMapper.findByPhone("81234567")).thenReturn(existingMerchant);
//
//        // When & Then
//        BusinessException exception = assertThrows(BusinessException.class,
//            () -> merchantService.registerMerchant(dto));
//
//        assertEquals(ResultStatus.USER_HAS_EXISTED, exception.getErrInfo());
//        verify(merchantMapper, never()).insert(any());
//    }
//
//    @Test
//    void testGetAllApprovedMerchants_Success() {
//        // Given
//        Merchant merchant1 = new Merchant();
//        merchant1.setId(1);
//        merchant1.setName("商家1");
//        merchant1.setStatus("approved");
//
//        Merchant merchant2 = new Merchant();
//        merchant2.setId(2);
//        merchant2.setName("商家2");
//        merchant2.setStatus("approved");
//
//        List<Merchant> merchants = Arrays.asList(merchant1, merchant2);
//        when(merchantMapper.selectList(any(QueryWrapper.class))).thenReturn(merchants);
//
//        // When
//        List<MerchantDto> result = merchantService.getAllApprovedMerchants();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertEquals("商家1", result.get(0).getName());
//        verify(merchantMapper, times(1)).selectList(any(QueryWrapper.class));
//    }
//
//    @Test
//    void testGetMerchantById_Success() {
//        // Given
//        when(merchantMapper.selectById(1)).thenReturn(mockMerchant);
//
//        // When
//        MerchantDto result = merchantService.getMerchantById(1);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.getId());
//        assertEquals("测试商家", result.getName());
//        verify(merchantMapper, times(1)).selectById(1);
//    }
//
//    @Test
//    void testGetMerchantById_NotFound() {
//        // Given
//        when(merchantMapper.selectById(999)).thenReturn(null);
//
//        // When
//        MerchantDto result = merchantService.getMerchantById(999);
//
//        // Then
//        assertNull(result);
//    }
//
//    @Test
//    void testFindByUserId_Success() {
//        // Given
//        when(merchantMapper.selectOne(any(QueryWrapper.class))).thenReturn(mockMerchant);
//
//        // When
//        MerchantDto result = merchantService.findByUserId(1);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.getId());
//        verify(merchantMapper, times(1)).selectOne(any(QueryWrapper.class));
//    }
//
//    @Test
//    void testFindByUserId_NotFound() {
//        // Given
//        when(merchantMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
//
//        // When
//        MerchantDto result = merchantService.findByUserId(999);
//
//        // Then
//        assertNull(result);
//    }
//
//    @Test
//    void testUpdateMerchantProfile_Success() {
//        // Given
//        MerchantUpdateDto dto = new MerchantUpdateDto();
//        dto.setId(1);
//        dto.setName("更新后的商家名");
//        dto.setPhone("81234567");
//        dto.setAddress("新地址");
//
//        MerchantDto currentMerchant = new MerchantDto();
//        currentMerchant.setId(1);
//
//        when(userContextHolder.getCurrentUser()).thenReturn(mockUserContext);
//        when(merchantMapper.selectOne(any(QueryWrapper.class))).thenReturn(mockMerchant);
//        when(merchantMapper.findByPhone("81234567")).thenReturn(null);
//        when(merchantMapper.selectById(1)).thenReturn(mockMerchant);
//        when(merchantMapper.updateById(any(Merchant.class))).thenReturn(1);
//
//        // When
//        assertDoesNotThrow(() -> merchantService.updateMerchantProfile(dto, mockUserContext));
//
//        // Then
//        verify(merchantMapper, times(1)).updateById(any(Merchant.class));
//    }
//
//    @Test
//    void testUpdateMerchantProfile_Failed_NotMerchantRole() {
//        // Given
//        UserContext nonMerchantContext = new UserContext();
//        nonMerchantContext.setId(1);
//        nonMerchantContext.setRole("USER"); // 不是 MERCHANT 角色
//
//        MerchantUpdateDto dto = new MerchantUpdateDto();
//        dto.setId(1);
//
//        // When & Then
//        BusinessException exception = assertThrows(BusinessException.class,
//            () -> merchantService.updateMerchantProfile(dto, nonMerchantContext));
//
//        assertEquals(ResultStatus.ACCESS_DENIED, exception.getErrInfo());
//    }
//
//    @Test
//    void testUpdateMerchantProfile_Failed_PhoneAlreadyExists() {
//        // Given
//        mockUserContext.setRole("MERCHANT");
//        MerchantUpdateDto dto = new MerchantUpdateDto();
//        dto.setId(1);
//        dto.setPhone("81234567");
//
//        Merchant existingMerchantWithPhone = new Merchant();
//        existingMerchantWithPhone.setId(2); // 不同的商家ID
//
//        MerchantDto currentMerchant = new MerchantDto();
//        currentMerchant.setId(1);
//
//        when(userContextHolder.getCurrentUser()).thenReturn(mockUserContext);
//        when(merchantMapper.selectOne(any(QueryWrapper.class))).thenReturn(mockMerchant);
//        when(merchantMapper.findByPhone("81234567")).thenReturn(existingMerchantWithPhone);
//
//        // When & Then
//        BusinessException exception = assertThrows(BusinessException.class,
//            () -> merchantService.updateMerchantProfile(dto, mockUserContext));
//
//        assertEquals(ResultStatus.USER_HAS_EXISTED, exception.getErrInfo());
//    }
//
//    @Test
//    void testHandleRegisterResult_Approved() {
//        // Given
//        MerchantProcessedEvent event = new MerchantProcessedEvent(
//            1L, TaskStatus.APPROVED.getCode(), 1L, new Date(), "审核通过"
//        );
//
//        when(merchantMapper.selectOne(any(QueryWrapper.class))).thenReturn(mockMerchant);
//        when(userMapper.update(any(), any())).thenReturn(1);
//        when(merchantMapper.updateById(any(Merchant.class))).thenReturn(1);
//
//        // Mock 用户查询：第一次返回原用户，第二次返回更新后的用户（用于缓存更新）
//        User updatedUser = new User();
//        updatedUser.setId(1);
//        updatedUser.setUsername("testuser");
//        updatedUser.setRole("MERCHANT");
//        when(userMapper.selectById(1)).thenReturn(mockUser).thenReturn(updatedUser);
//
//        // When
//        merchantService.handleRegisterResult(event);
//
//        // Then
//        verify(merchantMapper, times(1)).updateById(any(Merchant.class));
//        verify(userMapper, times(1)).update(any(), any());
//        verify(userCacheService, times(1)).deleteUserCache(anyString());
//
//        ArgumentCaptor<Merchant> merchantCaptor = ArgumentCaptor.forClass(Merchant.class);
//        verify(merchantMapper).updateById(merchantCaptor.capture());
//        assertEquals("approved", merchantCaptor.getValue().getStatus());
//    }
//
//    @Test
//    void testHandleRegisterResult_Rejected() {
//        // Given
//        MerchantProcessedEvent event = new MerchantProcessedEvent(
//            1L, TaskStatus.REJECTED.getCode(), 1L, new Date(), "审核拒绝"
//        );
//
//        when(merchantMapper.selectOne(any(QueryWrapper.class))).thenReturn(mockMerchant);
//        when(merchantMapper.updateById(any(Merchant.class))).thenReturn(1);
//
//        // When
//        merchantService.handleRegisterResult(event);
//
//        // Then
//        verify(merchantMapper, times(1)).updateById(any(Merchant.class));
//        verify(userMapper, never()).update(any(), any()); // 拒绝时不应该更新用户角色
//
//        ArgumentCaptor<Merchant> merchantCaptor = ArgumentCaptor.forClass(Merchant.class);
//        verify(merchantMapper).updateById(merchantCaptor.capture());
//        assertEquals("rejected", merchantCaptor.getValue().getStatus());
//    }
//}
//
