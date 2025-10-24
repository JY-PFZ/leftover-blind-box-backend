package nus.iss.se.magicbag.service.impl;

// --- 省略了 imports ---
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor; // 🟢 确保使用 @RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.common.constant.ResultStatus;
import nus.iss.se.magicbag.common.exception.BusinessException;
import nus.iss.se.magicbag.dto.*;
import nus.iss.se.magicbag.entity.*;
import nus.iss.se.magicbag.interfacemethods.CartInterface;
import nus.iss.se.magicbag.mapper.*;
import nus.iss.se.magicbag.service.IOrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Objects;

// 🟢 导入 Spring Security 相关的类
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils; // 🟢 确保导入 StringUtils


@Slf4j
@Service
@RequiredArgsConstructor // 🟢 使用 Lombok 自动生成构造函数
public class OrderServiceImpl implements IOrderService {

    // 🟢 使用 final 配合 @RequiredArgsConstructor
    private final OrderMapper orderMapper;
    private final OrderVerificationMapper orderVerificationMapper;
    private final UserMapper userMapper;
    private final MagicBagMapper magicBagMapper;
    private final MerchantMapper merchantMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartInterface cartService;
    // private final UserContextHolder userContextHolder; // 通常 Service 层不直接依赖 Holder

    /**
     * 🟢 辅助方法：从 SecurityContextHolder 安全地获取当前用户角色
     */
    private String getRoleFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getAuthorities() == null) {
            log.warn("--- [SECURITY_DEBUG] Authentication object or authorities are NULL in getRoleFromSecurityContext ---");
            return null;
        }

        // log.info("--- [SECURITY_DEBUG] Authentication Principal: {}", authentication.getPrincipal());
        // log.info("--- [SECURITY_DEBUG] Authentication Authorities (Raw): {}", authentication.getAuthorities());

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(r -> {
                    // log.info("--- [SECURITY_DEBUG] Found raw Authority: {}", r);
                    String role = r.startsWith("ROLE_") ? r.substring(5) : r;
                    // log.info("--- [SECURITY_DEBUG] Extracted Role: {}", role);
                    return role;
                })
                .orElseGet(() -> {
                    log.warn("--- [SECURITY_DEBUG] No authorities found for user. ---");
                    return null;
                });
    }

    @Override
    public IPage<OrderDto> getOrders(UserContext currentUser, OrderQueryDto queryDto) {
        // log.info("--- [DEBUG] 正在进入 getOrders 方法。传入的 UserContext ID: {}", currentUser != null ? currentUser.getId() : "NULL");

        String userRole = getRoleFromSecurityContext();
        // log.info("--- [DEBUG] 从 getRoleFromSecurityContext 返回的 Role: {}", userRole);

        if (userRole == null) {
            log.error("--- [DEBUG] userRole 为 NULL。即将抛出 ACCESS_DENIED (80005)。 ---");
            throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }

        // 检查 currentUser 是否有效
        if (currentUser == null || currentUser.getId() == null) {
            log.error("--- [DEBUG] UserContext 为 NULL 或 User ID 为 NULL。无法继续查询订单。 ---");
            throw new BusinessException(ResultStatus.USER_NOT_LOGGED_IN); // 或者更合适的错误
        }
        Integer currentUserId = currentUser.getId();

        Page<OrderDto> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());

        IPage<OrderDto> orderPage;
        // log.info("--- [DEBUG] 正在进入 switch 语句，使用 Role: {}", userRole);

        switch (userRole.toUpperCase()) { // 转换为大写以确保匹配
            case "SUPER_ADMIN":
            case "ADMIN":
                // log.info("--- [DEBUG] 执行 ADMIN/SUPER_ADMIN 查询 ---");
                orderPage = orderMapper.findAllOrders(page);
                break;
            case "MERCHANT":
                // log.info("--- [DEBUG] 执行 MERCHANT 查询 for user ID: {} ---", currentUserId);
                QueryWrapper<Merchant> merchantWrapper = new QueryWrapper<>();
                merchantWrapper.eq("user_id", currentUserId);
                Merchant merchant = merchantMapper.selectOne(merchantWrapper);
                if (merchant == null) {
                    log.error("--- [DEBUG] 未找到 user_id 为 {} 的商家记录。---", currentUserId);
                    throw new BusinessException(ResultStatus.MERCHANT_NOT_FOUND, "Merchant context not found for current user.");
                }
                // log.info("--- [DEBUG] 找到商家 ID: {}。开始查询商家订单。---", merchant.getId());
                orderPage = orderMapper.findByMerchantId(page, merchant.getId());
                break;
            case "CUSTOMER": // 同时处理 USER 和 CUSTOMER
            case "USER":
                // log.info("--- [DEBUG] 执行 USER/CUSTOMER 查询 for user ID: {} ---", currentUserId);
                orderPage = orderMapper.findByUserId(page, currentUserId);
                break;
            default:
                log.warn("--- [DEBUG] Role '{}' 没有匹配到任何 case。即将抛出 ACCESS_DENIED (80005)。 ---", userRole);
                throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }

        // ... [后续填充 OrderItems 的逻辑保持不变] ...
        if (orderPage != null && orderPage.getRecords() != null) {
            orderPage.getRecords().forEach(orderDto -> {
                // log.info("--- [DEBUG] Processing Order DTO ID: {}", orderDto.getId());
                if ("cart".equalsIgnoreCase(orderDto.getOrderType())) {
                    List<OrderItem> items = orderItemMapper.findByOrderId(orderDto.getId());
                    if (items != null) {
                        // log.info("--- [DEBUG] Found {} items for cart order ID: {}", items.size(), orderDto.getId());
                        orderDto.setOrderItems(items.stream()
                                .map(this::convertToOrderItemDto)
                                .collect(Collectors.toList()));
                    } else {
                        // log.info("--- [DEBUG] No items found for cart order ID: {}", orderDto.getId());
                        orderDto.setOrderItems(new ArrayList<>());
                    }
                }
                // ... (填充 bagTitle, merchantName, userName) ...
                if (orderDto.getBagId() != null) {
                    MagicBag bag = magicBagMapper.selectById(orderDto.getBagId());
                    if (bag != null) {
                        orderDto.setBagTitle(bag.getTitle());
                        Merchant m = merchantMapper.selectById(bag.getMerchantId());
                        if (m != null) {
                            orderDto.setMerchantName(m.getName());
                        }
                    }
                } else if ("cart".equalsIgnoreCase(orderDto.getOrderType()) && orderDto.getOrderItems() != null && !orderDto.getOrderItems().isEmpty()){
                    OrderItemDto firstItemDto = orderDto.getOrderItems().get(0);
                    if (firstItemDto != null && firstItemDto.getMagicBagId() != null) {
                        MagicBag bag = magicBagMapper.selectById(firstItemDto.getMagicBagId());
                        if (bag != null) {
                            orderDto.setBagTitle("Multiple Items");
                            Merchant m = merchantMapper.selectById(bag.getMerchantId());
                            if (m != null) {
                                orderDto.setMerchantName(m.getName());
                            }
                        }
                    }
                }
                User user = userMapper.selectById(orderDto.getUserId());
                if (user != null) {
                    orderDto.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                }
            });
        }

        return orderPage;
    }

    // --- getOrderDetail, updateOrderStatus, cancelOrder, verifyOrder, getOrderStats ---
    // --- buildOrderDetailResponse, convertToVerificationDto 保持不变 (省略) ---
    // 确保这些方法内部也使用 getRoleFromSecurityContext()

    @Override
    public OrderDetailResponse getOrderDetail(Integer orderId, UserContext currentUser) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultStatus.ORDER_NOT_FOUND);
        }

        String userRole = getRoleFromSecurityContext(); // 🟢 使用辅助方法
        Integer currentUserId = currentUser.getId(); // .getId() 是可靠的

        if (userRole == null) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED, "Role not found in context.");
        }

        boolean allowed = false;
        switch (userRole.toUpperCase()) { // 转换为大写
            case "SUPER_ADMIN":
            case "ADMIN":
                allowed = true;
                break;
            case "MERCHANT":
                // ... (查找 merchantIdToCheck 的逻辑) ...
                Integer merchantIdToCheck = null;
                if (order.getBagId() != null) {
                    MagicBag bag = magicBagMapper.selectById(order.getBagId());
                    if (bag != null) merchantIdToCheck = bag.getMerchantId();
                } else if ("cart".equalsIgnoreCase(order.getOrderType())) {
                    List<OrderItem> items = orderItemMapper.findByOrderId(orderId);
                    if (items != null && !items.isEmpty()) {
                        MagicBag bag = magicBagMapper.selectById(items.get(0).getMagicBagId());
                        if (bag != null) merchantIdToCheck = bag.getMerchantId();
                    }
                }

                QueryWrapper<Merchant> merchantWrapper = new QueryWrapper<>();
                merchantWrapper.eq("user_id", currentUserId);
                Merchant currentMerchant = merchantMapper.selectOne(merchantWrapper);

                if (currentMerchant != null && merchantIdToCheck != null && merchantIdToCheck.equals(currentMerchant.getId())) {
                    allowed = true;
                }
                break;
            case "CUSTOMER": // 同时处理 USER 和 CUSTOMER
            case "USER":
                if (order.getUserId().equals(currentUserId)) {
                    allowed = true;
                }
                break;
        }

        if (!allowed) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }

        return buildOrderDetailResponse(order);
    }


    @Override
    @Transactional
    public void updateOrderStatus(Integer orderId, OrderStatusUpdateDto statusDto, UserContext currentUser) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultStatus.ORDER_NOT_FOUND);
        }

        String userRole = getRoleFromSecurityContext(); // 🟢 使用辅助方法
        Integer currentUserId = currentUser.getId();

        if (userRole == null) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED, "Role not found in context.");
        }

        boolean allowed = false;
        switch(userRole.toUpperCase()) { // 转大写
            case "SUPER_ADMIN":
            case "ADMIN":
                allowed = true;
                break;
            case "MERCHANT":
                // ... (查找 merchantIdToCheck 的逻辑) ...
                Integer merchantIdToCheck = null;
                if (order.getBagId() != null) {
                    MagicBag bag = magicBagMapper.selectById(order.getBagId());
                    if (bag != null) merchantIdToCheck = bag.getMerchantId();
                } else if ("cart".equalsIgnoreCase(order.getOrderType())) {
                    List<OrderItem> items = orderItemMapper.findByOrderId(orderId);
                    if (items != null && !items.isEmpty()) {
                        MagicBag bag = magicBagMapper.selectById(items.get(0).getMagicBagId());
                        if (bag != null) merchantIdToCheck = bag.getMerchantId();
                    }
                }

                QueryWrapper<Merchant> merchantWrapper = new QueryWrapper<>();
                merchantWrapper.eq("user_id", currentUserId);
                Merchant currentMerchant = merchantMapper.selectOne(merchantWrapper);
                if (currentMerchant != null && merchantIdToCheck != null && merchantIdToCheck.equals(currentMerchant.getId())) {
                    allowed = true;
                }
                break;
        }


        if (!allowed) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }

        order.setStatus(statusDto.getStatus());

        Date now = new Date();
        switch (statusDto.getStatus().toLowerCase()) {
            case "paid":
                if (order.getPaidAt() == null) order.setPaidAt(now);
                break;
            case "completed":
                if (order.getCompletedAt() == null) order.setCompletedAt(now);
                break;
            case "cancelled":
                if (order.getCancelledAt() == null) order.setCancelledAt(now);
                break;
        }

        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Integer orderId, UserContext currentUser) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultStatus.ORDER_NOT_FOUND);
        }

        String userRole = getRoleFromSecurityContext(); // 🟢 使用辅助方法
        Integer currentUserId = currentUser.getId();

        if (userRole == null) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED, "Role not found in context.");
        }

        boolean allowed = false;
        switch(userRole.toUpperCase()) { // 转大写
            case "SUPER_ADMIN":
            case "ADMIN":
                allowed = true;
                break;
            case "CUSTOMER": // 同时处理
            case "USER":
                if (order.getUserId().equals(currentUserId)) {
                    allowed = true;
                }
                break;
        }


        if (!allowed) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }

        if ("completed".equalsIgnoreCase(order.getStatus())) {
            throw new BusinessException(ResultStatus.ORDER_CANNOT_CANCEL);
        }
        if ("cancelled".equalsIgnoreCase(order.getStatus())) {
            throw new BusinessException(ResultStatus.ORDER_ALREADY_CANCELLED);
        }

        order.setStatus("cancelled");
        order.setCancelledAt(new Date());
        orderMapper.updateById(order);
    }


    @Override
    @Transactional
    public void verifyOrder(Integer orderId, OrderVerificationDto verificationDto, UserContext currentUser) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultStatus.ORDER_NOT_FOUND);
        }

        String userRole = getRoleFromSecurityContext(); // 🟢 使用辅助方法

        if (!"MERCHANT".equalsIgnoreCase(userRole)) { // 忽略大小写
            throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }

        // ... (查找 merchantIdToCheck 的逻辑) ...
        Integer merchantIdToCheck = null;
        if (order.getBagId() != null) {
            MagicBag bag = magicBagMapper.selectById(order.getBagId());
            if (bag != null) merchantIdToCheck = bag.getMerchantId();
        } else if ("cart".equalsIgnoreCase(order.getOrderType())) {
            List<OrderItem> items = orderItemMapper.findByOrderId(orderId);
            if (items != null && !items.isEmpty()) {
                MagicBag bag = magicBagMapper.selectById(items.get(0).getMagicBagId());
                if (bag != null) merchantIdToCheck = bag.getMerchantId();
            }
        }

        QueryWrapper<Merchant> merchantWrapper = new QueryWrapper<>();
        merchantWrapper.eq("user_id", currentUser.getId());
        Merchant currentMerchant = merchantMapper.selectOne(merchantWrapper);

        if (currentMerchant == null || merchantIdToCheck == null || !merchantIdToCheck.equals(currentMerchant.getId())) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }

        if (!"paid".equalsIgnoreCase(order.getStatus())) {
            throw new BusinessException(ResultStatus.ORDER_CANNOT_VERIFY);
        }

        QueryWrapper<OrderVerification> verificationWrapper = new QueryWrapper<>();
        verificationWrapper.eq("order_id", orderId);
        if (orderVerificationMapper.selectCount(verificationWrapper) > 0) {
            throw new BusinessException(ResultStatus.DATA_ALREADY_EXISTED, "Order has already been verified.");
        }


        OrderVerification verification = new OrderVerification();
        verification.setOrderId(orderId);
        verification.setVerifiedBy(currentMerchant.getId()); // 使用商家主键ID
        verification.setLocation(verificationDto.getLocation());
        // verification.setVerifiedAt(new Date()); // verification 实体应该自动设置时间戳
        orderVerificationMapper.insert(verification);

        order.setStatus("completed");
        order.setCompletedAt(new Date());
        orderMapper.updateById(order);
    }

    @Override
    public OrderStatsDto getOrderStats(UserContext currentUser) {
        String userRole = getRoleFromSecurityContext(); // 🟢 使用辅助方法
        Integer currentUserId = currentUser.getId();

        if (userRole == null) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED, "Role not found in context.");
        }

        switch (userRole.toUpperCase()) { // 转大写
            case "SUPER_ADMIN":
            case "ADMIN":
                return orderMapper.findAllOrderStats();
            case "MERCHANT":
                QueryWrapper<Merchant> merchantWrapper = new QueryWrapper<>();
                merchantWrapper.eq("user_id", currentUserId);
                Merchant merchant = merchantMapper.selectOne(merchantWrapper);
                if (merchant == null) {
                    return new OrderStatsDto();
                }
                return orderMapper.findOrderStatsByMerchantId(merchant.getId());
            case "CUSTOMER": // 同时处理
            case "USER":
                return orderMapper.findOrderStatsByUserId(currentUserId);
            default:
                throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }
    }


    private OrderDetailResponse buildOrderDetailResponse(Order order) {
        OrderDetailResponse response = new OrderDetailResponse();
        OrderDto orderDto = convertToOrderDto(order); // convertToOrderDto 会填充 orderItems
        response.setOrder(orderDto);
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            OrderDetailResponse.UserInfo userInfo = new OrderDetailResponse.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setNickname(user.getNickname());
            userInfo.setPhone(user.getPhone());
            response.setUser(userInfo);
        }

        OrderDetailResponse.MerchantInfo merchantInfo = null;
        // 尝试从 orderDto 中获取商家信息 (如果 convertToOrderDto 填充了)
        // String merchantNameFromDto = orderDto.getMerchantName();
        // if (StringUtils.hasText(merchantNameFromDto)) {
        // 仅有名字可能不够，需要 ID 和其他信息
        // }

        // 如果没有，或者需要完整信息，则重新查询
        Integer merchantIdToQuery = null;
        if ("single".equalsIgnoreCase(order.getOrderType()) && order.getBagId() != null) {
            MagicBag magicBag = magicBagMapper.selectById(order.getBagId());
            if (magicBag != null) {
                merchantIdToQuery = magicBag.getMerchantId();
                // 设置 MagicBagInfo (如果需要)
                OrderDetailResponse.MagicBagInfo bagInfo = new OrderDetailResponse.MagicBagInfo();
                BeanUtils.copyProperties(magicBag, bagInfo);
                response.setMagicBag(bagInfo);
            }
        } else if ("cart".equalsIgnoreCase(order.getOrderType()) && orderDto.getOrderItems() != null && !orderDto.getOrderItems().isEmpty()) {
            OrderItemDto firstItem = orderDto.getOrderItems().get(0);
            if (firstItem != null && firstItem.getMagicBagId() != null) {
                MagicBag firstBag = magicBagMapper.selectById(firstItem.getMagicBagId());
                if (firstBag != null) {
                    merchantIdToQuery = firstBag.getMerchantId();
                }
            }
        }

        if (merchantIdToQuery != null) {
            Merchant merchant = merchantMapper.selectById(merchantIdToQuery);
            if (merchant != null) {
                merchantInfo = new OrderDetailResponse.MerchantInfo();
                BeanUtils.copyProperties(merchant, merchantInfo);
            }
        }
        response.setMerchant(merchantInfo);


        QueryWrapper<OrderVerification> verificationWrapper = new QueryWrapper<>();
        verificationWrapper.eq("order_id", order.getId());
        List<OrderVerification> verifications = orderVerificationMapper.selectList(verificationWrapper);

        if (verifications != null) {
            List<OrderVerificationDto> verificationDtos = verifications.stream()
                    .map(this::convertToVerificationDto)
                    .collect(Collectors.toList());
            response.setVerifications(verificationDtos);
        } else {
            response.setVerifications(new ArrayList<>());
        }


        return response;
    }


    private OrderVerificationDto convertToVerificationDto(OrderVerification verification) {
        if (verification == null) return null;
        OrderVerificationDto dto = new OrderVerificationDto();
        dto.setId(verification.getId());
        dto.setOrderId(verification.getOrderId());
        dto.setVerifiedBy(verification.getVerifiedBy());
        dto.setVerifiedAt(verification.getVerifiedAt());
        dto.setLocation(verification.getLocation());
        return dto;
    }


    @Override
    @Transactional
    public OrderDto createOrderFromCart(Integer userId) {
        // 🟢 添加 DEBUG 日志
        log.info("[CREATE_ORDER_DEBUG] Attempting to create order from cart for userId: {}", userId);

        CartDto cart = cartService.getActiveCart(userId);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            log.error("[CREATE_ORDER_DEBUG] Cart is empty or invalid for userId: {}", userId);
            throw new BusinessException(ResultStatus.DATA_IS_WRONG, "Cart is empty or invalid.");
        }
        // 🟢 打印获取到的 CartDto 内容
        log.info("[CREATE_ORDER_DEBUG] Retrieved CartDto: {}", cart);


        CartItemDto firstCartItem = cart.getItems().get(0);
        // 🟢 检查 firstCartItem 和 magicbagId
        if (firstCartItem == null || firstCartItem.getMagicbagId() == null) {
            log.error("[CREATE_ORDER_DEBUG] First cart item or its magicbagId is null. Item: {}", firstCartItem);
            throw new BusinessException(ResultStatus.DATA_IS_WRONG, "First cart item data is invalid.");
        }
        log.info("[CREATE_ORDER_DEBUG] First cart item MagicBagId: {}", firstCartItem.getMagicbagId());

        MagicBag firstBag = magicBagMapper.selectById(firstCartItem.getMagicbagId());
        if (firstBag == null) {
            log.error("[CREATE_ORDER_DEBUG] Could not find MagicBag details for the first cart item's ID: {}", firstCartItem.getMagicbagId());
            throw new BusinessException(ResultStatus.DATA_IS_WRONG, "Could not find MagicBag details for the first cart item.");
        }
        LocalTime pickupStartTime = firstBag.getPickupStartTime();
        LocalTime pickupEndTime = firstBag.getPickupEndTime();
        LocalDate today = LocalDate.now();
        Date startDateTime = Date.from(pickupStartTime.atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        Date endDateTime = Date.from(pickupEndTime.atDate(today).atZone(ZoneId.systemDefault()).toInstant());

        BigDecimal totalPrice = BigDecimal.ZERO;
        // 循环 1: 检查库存和计算总价
        for (CartItemDto item : cart.getItems()) {
            // 🟢 详细日志记录每个 item
            log.info("[CREATE_ORDER_DEBUG] Processing CartItem: {}", item);
            if (item == null) {
                log.error("[CREATE_ORDER_DEBUG] Encountered NULL item in cart items list.");
                throw new BusinessException(ResultStatus.DATA_IS_WRONG, "Cart contains invalid item data.");
            }
            // 🟢 在使用前检查 magicbagId 和 quantity
            Integer currentMagicBagId = item.getMagicbagId();
            Integer currentQuantity = item.getQuantity();
            if (currentMagicBagId == null) {
                log.error("[CREATE_ORDER_DEBUG] CartItem is missing magicbagId. Item: {}", item);
                throw new BusinessException(ResultStatus.DATA_IS_WRONG, "Cart item is missing product ID.");
            }
            if (currentQuantity == null || currentQuantity <= 0) {
                log.error("[CREATE_ORDER_DEBUG] CartItem has invalid quantity ({}). Item: {}", currentQuantity, item);
                throw new BusinessException(ResultStatus.DATA_IS_WRONG, "Cart item has invalid quantity.");
            }
            log.info("[CREATE_ORDER_DEBUG] Checking stock for MagicBagId: {}, Quantity: {}", currentMagicBagId, currentQuantity);


            MagicBag bag = magicBagMapper.selectById(currentMagicBagId);
            if (bag == null ||  !bag.isActive()) {
                log.error("[CREATE_ORDER_DEBUG] Item not available or not active. MagicBagId: {}", currentMagicBagId);
                throw new BusinessException(ResultStatus.FAIL, "Item ".concat(item.getBagName() != null ? item.getBagName() : "#"+currentMagicBagId) + " is not available.");
            }
            if (bag.getQuantity() < currentQuantity) {
                log.error("[CREATE_ORDER_DEBUG] Insufficient stock for MagicBagId: {}. Available: {}, Requested: {}", currentMagicBagId, bag.getQuantity(), currentQuantity);
                throw new BusinessException(ResultStatus.FAIL, "Insufficient stock for ".concat(item.getBagName() != null ? item.getBagName() : "#"+currentMagicBagId));
            }
            if (bag.getPrice() == null) {
                log.error("[CREATE_ORDER_DEBUG] Price not set for MagicBagId: {}", currentMagicBagId);
                throw new BusinessException(ResultStatus.DATA_IS_WRONG, "Price not set for item ".concat(item.getBagName() != null ? item.getBagName() : "#"+currentMagicBagId));
            }
            // 🟢 添加日志记录价格计算
            try {
                BigDecimal itemPrice = BigDecimal.valueOf(bag.getPrice());
                BigDecimal itemQuantity = BigDecimal.valueOf(currentQuantity);
                BigDecimal subTotal = itemPrice.multiply(itemQuantity);
                totalPrice = totalPrice.add(subTotal);
                log.info("[CREATE_ORDER_DEBUG] Calculated subTotal for MagicBagId {}: {} * {} = {}. New total: {}",
                        currentMagicBagId, itemPrice, itemQuantity, subTotal, totalPrice);
            } catch (NumberFormatException e) {
                log.error("[CREATE_ORDER_DEBUG] NumberFormatException during price calculation for MagicBagId: {}. Price='{}', Quantity='{}'. Error: {}",
                        currentMagicBagId, bag.getPrice(), currentQuantity, e.getMessage());
                throw new BusinessException(ResultStatus.DATA_IS_WRONG, "Invalid price or quantity format for item.");
            } catch (Exception e) {
                log.error("[CREATE_ORDER_DEBUG] Unexpected error during price calculation for MagicBagId: {}. Error: {}", currentMagicBagId, e.getMessage(), e);
                throw new BusinessException(ResultStatus.FAIL, "Error calculating total price.");
            }
        }
        log.info("[CREATE_ORDER_DEBUG] Total price calculated: {}", totalPrice);

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setOrderType("cart");
        order.setTotalPrice(totalPrice);
        order.setStatus("pending");
        order.setPickupCode(generatePickupCode());
        order.setCreatedAt(new Date());
        order.setPickupStartTime(startDateTime);
        order.setPickupEndTime(endDateTime);

        int insertedRows = orderMapper.insert(order);
        if (insertedRows == 0 || order.getId() == null) {
            log.error("[CREATE_ORDER_DEBUG] Failed to insert main order record for userId: {}", userId);
            throw new BusinessException(ResultStatus.FAIL, "Failed to create main order record.");
        }
        log.info("[CREATE_ORDER_DEBUG] Main order created successfully, ID: {}", order.getId());


        // 循环 2: 创建订单项并更新库存
        for (CartItemDto item : cart.getItems()) {
            // 🟢 再次检查 item 和 ID/数量
            if (item == null || item.getMagicbagId() == null) { // 步骤 1: 先检查 null
                log.error("[CREATE_ORDER_DEBUG] Invalid item data (null fields) found before creating OrderItem. Item: {}", item);
                throw new BusinessException(ResultStatus.DATA_IS_WRONG, "Invalid cart item data (null fields).");
            }
            Integer currentMagicBagId = item.getMagicbagId();
            Integer currentQuantity = item.getQuantity();
            log.info("[CREATE_ORDER_DEBUG] Creating OrderItem for MagicBagId: {}, Quantity: {}", currentMagicBagId, currentQuantity);


            MagicBag bag = magicBagMapper.selectById(currentMagicBagId);
            // 理论上 bag 不会是 null，因为循环1已经检查过，但以防万一
            if (bag == null || bag.getPrice() == null) {
                log.error("[CREATE_ORDER_DEBUG] Could not find MagicBag or its price while creating OrderItem. MagicBagId: {}", currentMagicBagId);
                throw new BusinessException(ResultStatus.DATA_IS_WRONG, "Could not retrieve price for item ".concat(item.getBagName() != null ? item.getBagName() : "#"+currentMagicBagId));
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setMagicBagId(currentMagicBagId); // 使用 Integer
            orderItem.setQuantity(currentQuantity);     // 使用 Integer
            try {
                BigDecimal unitPrice = BigDecimal.valueOf(bag.getPrice());
                BigDecimal quantityBD = BigDecimal.valueOf(currentQuantity);
                orderItem.setUnitPrice(unitPrice);
                orderItem.setSubtotal(unitPrice.multiply(quantityBD));
            } catch (NumberFormatException e) {
                log.error("[CREATE_ORDER_DEBUG] NumberFormatException setting OrderItem price/subtotal for MagicBagId: {}. Price='{}', Quantity='{}'. Error: {}",
                        currentMagicBagId, bag.getPrice(), currentQuantity, e.getMessage());
                throw new BusinessException(ResultStatus.DATA_IS_WRONG, "Invalid price or quantity format for order item.");
            } catch (Exception e) {
                log.error("[CREATE_ORDER_DEBUG] Unexpected error setting OrderItem price/subtotal for MagicBagId: {}. Error: {}", currentMagicBagId, e.getMessage(), e);
                throw new BusinessException(ResultStatus.FAIL, "Error setting order item details.");
            }

            orderItemMapper.insert(orderItem);
            log.info("[CREATE_ORDER_DEBUG] OrderItem inserted successfully for MagicBagId: {}", currentMagicBagId);

            // 更新库存
            int updatedStock = bag.getQuantity() - currentQuantity;
            if (updatedStock < 0) {
                log.error("[CREATE_ORDER_DEBUG] Concurrent stock update conflict for MagicBagId: {}. Current Stock: {}, Requested: {}", currentMagicBagId, bag.getQuantity(), currentQuantity);
                throw new BusinessException(ResultStatus.FAIL, "Concurrent stock update conflict for ".concat(item.getBagName() != null ? item.getBagName() : "#"+currentMagicBagId));
            }
            bag.setQuantity(updatedStock);
            magicBagMapper.updateById(bag);
            log.info("[CREATE_ORDER_DEBUG] Updated stock for MagicBagId {}: Decreased by {}, Remaining: {}", currentMagicBagId, currentQuantity, updatedStock);
        }
        log.info("[CREATE_ORDER_DEBUG] All order items created successfully for Order ID: {}", order.getId());

        cartService.clearCart(userId);
        log.info("[CREATE_ORDER_DEBUG] Cart cleared successfully for User ID: {}", userId);


        Order createdOrder = orderMapper.selectById(order.getId());
        return convertToOrderDto(createdOrder);
    }


    private String generateOrderNo() {
        // 保持不变
        return "MB" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }


    private String generatePickupCode() {
        // 保持不变
        return String.format("%04d", (int) (Math.random() * 10000));
    }


    private OrderDto convertToOrderDto(Order order) {
        if (order == null) return null;
        OrderDto dto = new OrderDto();
        BeanUtils.copyProperties(order, dto);

        if ("cart".equalsIgnoreCase(order.getOrderType())) {
            List<OrderItem> orderItems = orderItemMapper.findByOrderId(order.getId());
            if (orderItems != null) {
                dto.setOrderItems(orderItems.stream()
                        .map(this::convertToOrderItemDto)
                        .filter(Objects::nonNull) // 过滤掉转换失败的 DTO
                        .collect(Collectors.toList()));
            } else {
                dto.setOrderItems(new ArrayList<>());
            }
        }

        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            dto.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        }

        // 填充商家名称和商品标题 (逻辑保持不变)
        if ("single".equalsIgnoreCase(order.getOrderType()) && order.getBagId() != null) {
            MagicBag bag = magicBagMapper.selectById(order.getBagId());
            if (bag != null) {
                dto.setBagTitle(bag.getTitle());
                Merchant merchant = merchantMapper.selectById(bag.getMerchantId());
                if (merchant != null) dto.setMerchantName(merchant.getName());
            }
        } else if ("cart".equalsIgnoreCase(order.getOrderType()) && dto.getOrderItems() != null && !dto.getOrderItems().isEmpty()) {
            OrderItemDto firstItem = dto.getOrderItems().get(0);
            if (firstItem != null && firstItem.getMagicBagId() != null) {
                MagicBag bag = magicBagMapper.selectById(firstItem.getMagicBagId());
                if (bag != null) {
                    dto.setBagTitle("Multiple Items"); // 购物车订单统一标题
                    Merchant merchant = merchantMapper.selectById(bag.getMerchantId());
                    // 购物车可能来自不同商家，这里只显示第一个商品的商家，或者标记为 "Multiple Merchants"
                    if (merchant != null) dto.setMerchantName(merchant.getName() + " (and possibly others)");
                }
            }
        }

        return dto;
    }


    private OrderItemDto convertToOrderItemDto(OrderItem orderItem) {
        // 保持注释: // if (orderItem == null) return null;

        // 🟢 步骤 1: 添加一个显式的对象非空检查 (虽然理论上多余)
        if (orderItem == null) {
            log.warn("convertToOrderItemDto received null orderItem unexpectedly.");
            return null; // 或者抛出异常，因为这不该发生
        }

        OrderItemDto dto = new OrderItemDto();
        BeanUtils.copyProperties(orderItem, dto);

        Integer magicBagId = orderItem.getMagicBagId();

        // 🟢 步骤 2: 使用 Objects.isNull 进行 null 检查 (替代 == null)
        if (Objects.isNull(magicBagId)) {
            log.warn("OrderItem ID {} has a null MagicBag ID. Cannot fetch MagicBag details.", orderItem.getId());
            dto.setMagicBagTitle("Invalid Item (Missing ID)");
            return dto;
        }

        // 🟢 步骤 3: 确保在使用 magicBagId 前它是非空的 (虽然上面已经检查过)
        if(magicBagId != null) { // 这个 if 理论上也很多余，但有时能帮助编译器
            MagicBag magicBag = magicBagMapper.selectById(magicBagId);

            if (magicBag != null) {
                dto.setMagicBagTitle(magicBag.getTitle());
                dto.setMagicBagImageUrl(magicBag.getImageUrl());
                dto.setMagicBagCategory(magicBag.getCategory());
            } else {
                log.warn("无法为 OrderItem ID {} 找到关联的 MagicBag ID {}", orderItem.getId(), magicBagId);
                dto.setMagicBagTitle("Unknown Item (ID: " + magicBagId + ")");
            }
        } else {
            // 这个分支理论上永远不会执行
            log.error("Unexpected null magicBagId after null check for OrderItem ID {}", orderItem.getId());
            dto.setMagicBagTitle("Error processing item");
        }

        return dto;
    }

}

