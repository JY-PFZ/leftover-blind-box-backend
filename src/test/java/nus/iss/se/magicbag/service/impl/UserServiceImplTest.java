package nus.iss.se.magicbag.service.impl;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import lombok.SneakyThrows;
import nus.iss.se.magicbag.auth.service.UserCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.common.constant.UserStatus;
import nus.iss.se.magicbag.common.exception.BusinessException;
import nus.iss.se.magicbag.common.constant.ResultStatus;
import nus.iss.se.magicbag.dto.RegisterReq;
import nus.iss.se.magicbag.dto.UserDto;
import nus.iss.se.magicbag.entity.User;
import nus.iss.se.magicbag.mapper.UserMapper;
import nus.iss.se.magicbag.service.EmailService;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserCacheService userCacheService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl(passwordEncoder,userCacheService,emailService);
        // 通过反射设置 baseMapper（因为它是 protected）
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
    }

    // ========================
    // Test: register
    // ========================

    @SneakyThrows
    @Test
    void testRegister_UserAlreadyExists_ThrowsException() {
        RegisterReq req = new RegisterReq();
        req.setUsername("alice");
        req.setPassword("123456");
        req.setRole("CUSTOMER");

        when(userMapper.selectByUsername("alice")).thenReturn(new User());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(req);
        });

        assertEquals(ResultStatus.USER_HAS_EXISTED, exception.getErrInfo());
        verify(emailService, never()).sendActivationEmail(anyString());
    }

    @Test
    void testRegister_NewUser_Success() throws Exception {
        RegisterReq req = new RegisterReq();
        req.setUsername("bob");
        req.setPassword("123456");
        req.setRole("CUSTOMER");

        when(userMapper.selectByUsername("bob")).thenReturn(null);

        userService.register(req);

        verify(userMapper).insert(any(User.class));
        verify(emailService).sendActivationEmail("bob");
    }


    // ========================
    // Test: updateUserInfo (with UserContext)
    // ========================

    @Test
    void testUpdateUserInfo_IdMismatch_ThrowsAccessDenied() {
        UserDto dto = new UserDto();
        dto.setId(2);
        dto.setUsername("diana");

        UserContext currentUser = new UserContext();
        currentUser.setId(1); // 不同 ID

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.updateUserInfo(dto, currentUser);
        });

        assertEquals(ResultStatus.ACCESS_DENIED, exception.getErrInfo());
    }

    @Test
    void testUpdateUserInfo_InvalidRole_ThrowsAccessDenied() {
        UserDto dto = new UserDto();
        dto.setId(1);
        dto.setUsername("eve");

        UserContext currentUser = new UserContext();
        currentUser.setId(1);
        currentUser.setRole("ADMIN"); // 非 CUSTOMER/MERCHANT

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.updateUserInfo(dto, currentUser);
        });

        assertEquals(ResultStatus.ACCESS_DENIED, exception.getErrInfo());
    }

    @Test
    void testUpdateUserInfo_PhoneAlreadyUsed_ThrowsException() {
        UserDto dto = new UserDto();
        dto.setId(1);
        dto.setPhone("13800138000");

        UserContext currentUser = new UserContext();
        currentUser.setId(1);
        currentUser.setRole("CUSTOMER");

        // 模拟手机号已被其他用户使用
        User otherUser = new User();
        otherUser.setId(2);
        when(userMapper.selectByPhone("13800138000")).thenReturn(otherUser);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.updateUserInfo(dto, currentUser);
        });

        assertEquals(ResultStatus.USER_HAS_EXISTED, exception.getErrInfo());
    }

}