package nus.iss.se.magicbag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.auth.common.UserContextHolder;
import nus.iss.se.magicbag.common.constant.OrderStatus;
import nus.iss.se.magicbag.common.constant.ResultStatus; // 🟢 确保导入 ResultStatus
import nus.iss.se.magicbag.common.constant.UserRole;
import nus.iss.se.magicbag.common.exception.BusinessException;
import nus.iss.se.magicbag.dto.*;
import nus.iss.se.magicbag.entity.*;
import nus.iss.se.magicbag.service.ICartService;
import nus.iss.se.magicbag.mapper.*;
import nus.iss.se.magicbag.service.IOrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime; // 🟢 导入 LocalTime
import java.time.ZoneId; // 🟢 导入 ZoneId
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderMapper orderMapper;
    private final OrderVerificationMapper orderVerificationMapper;
    private final UserMapper userMapper;
    private final MagicBagMapper magicBagMapper;
    private final MerchantMapper merchantMapper;
    private final OrderItemMapper orderItemMapper;
    private final ICartService cartService;
    private final UserContextHolder userContextHolder;
    private final Random random = new Random();

    @Override
    public IPage<OrderDto> getOrders(UserContext currentUser, OrderQueryDto queryDto) {
        String userRole = currentUser.getRole();
        Page<OrderDto> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());

        IPage<OrderDto> orderPage;

        // 将字符串转换为枚举实例
        UserRole role = UserRole.getByCode(userRole)
                .orElseThrow(() -> new BusinessException(ResultStatus.ACCESS_DENIED));

        switch (role) {
            case SUPER_ADMIN, ADMIN:
                orderPage = orderMapper.findAllOrders(page);
                break;
            case MERCHANT:
                Integer merchantUserId = currentUser.getId();
                LambdaQueryWrapper<Merchant> merchantWrapper = new LambdaQueryWrapper<>();
                merchantWrapper.eq(Merchant::getUserId, merchantUserId);
                Merchant merchant = merchantMapper.selectOne(merchantWrapper);
                if (merchant == null) {
                    throw new BusinessException(ResultStatus.USER_NOT_FOUND, "Merchant user context not found.");
                }
                orderPage = orderMapper.findByMerchantId(page, merchant.getId());
                break;
            case USER, CUSTOMER:
                orderPage = orderMapper.findByUserId(page, currentUser.getId());
                break;
            default:
                throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }

        // 对查询结果进行处理，填充 OrderItems 和其他信息
        if (orderPage != null && orderPage.getRecords() != null) {
            orderPage.getRecords().forEach(orderDto -> {
                if ("cart".equalsIgnoreCase(orderDto.getOrderType())) {
                    List<OrderItem> items = orderItemMapper.findByOrderId(orderDto.getId());
                    orderDto.setOrderItems(items.stream()
                            .map(this::convertToOrderItemDto)
                            .toList());
                }
                if (orderDto.getBagId() != null) {
                    MagicBag bag = magicBagMapper.selectById(orderDto.getBagId());
                    if (bag != null) {
                        orderDto.setBagTitle(bag.getTitle());
                        Merchant m = merchantMapper.selectById(bag.getMerchantId());
                        if (m != null) {
                            orderDto.setMerchantName(m.getName());
                        }
                    }
                } else if ("cart".equalsIgnoreCase(orderDto.getOrderType()) && !CollectionUtils.isEmpty(orderDto.getOrderItems())){
                    OrderItemDto firstItemDto = orderDto.getOrderItems().getFirst();
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
                orderDto.setUserName(user != null &&user.getNickname() != null ? user.getNickname() : user.getUsername());
            });
        }

        return orderPage;
    }

    @Override
    public OrderDetailResponse getOrderDetail(Integer orderId, UserContext currentUser) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultStatus.ORDER_NOT_FOUND);
        }

        String userRole = currentUser.getRole();
        Integer currentUserId = currentUser.getId();

        boolean allowed = false;

        UserRole role = UserRole.getByCode(userRole)
                .orElseThrow(() -> new BusinessException(ResultStatus.ACCESS_DENIED));

        switch (role) {
            case SUPER_ADMIN, ADMIN:
                allowed = true;
                break;
            case MERCHANT:
                Integer merchantIdToCheck = null;
                if (order.getBagId() != null) {
                    MagicBag bag = magicBagMapper.selectById(order.getBagId());
                    if (bag != null) merchantIdToCheck = bag.getMerchantId();
                } else if ("cart".equalsIgnoreCase(order.getOrderType())) {
                    List<OrderItem> items = orderItemMapper.findByOrderId(orderId);
                    if (items != null && !items.isEmpty()) {
                        MagicBag bag = magicBagMapper.selectById(items.getFirst().getMagicBagId());
                        if (bag != null) merchantIdToCheck = bag.getMerchantId();
                    }
                }
                LambdaQueryWrapper<Merchant> merchantWrapper = new LambdaQueryWrapper<>();
                merchantWrapper.eq(Merchant::getUserId, currentUserId);
                Merchant currentMerchant = merchantMapper.selectOne(merchantWrapper);

                if (currentMerchant != null && merchantIdToCheck != null && merchantIdToCheck.equals(currentMerchant.getId())) {
                    allowed = true;
                }
                break;
            case USER, CUSTOMER:
                if (order.getUserId().equals(currentUserId)) {
                    allowed = true;
                }
                break;
            default:
                throw new BusinessException(ResultStatus.ACCESS_DENIED);
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

        String userRole = currentUser.getRole();
        Integer currentUserId = currentUser.getId();

        boolean allowed = false;
        if (UserRole.SUPER_ADMIN.getCode().equals(userRole) || UserRole.ADMIN.getCode().equals(userRole)) {
            allowed = true;
        } else if (UserRole.MERCHANT.getCode().equals(userRole)) {
            MagicBag bag = null;
            if (order.getBagId() != null) {
                bag = magicBagMapper.selectById(order.getBagId());
            } else if ("cart".equalsIgnoreCase(order.getOrderType())) {
                List<OrderItem> items = orderItemMapper.findByOrderId(orderId);
                if (items != null && !items.isEmpty()) {
                    bag = magicBagMapper.selectById(items.getFirst().getMagicBagId());
                }
            }

            Integer merchantIdToCheck = bag != null ? bag.getMerchantId() : null;

            LambdaQueryWrapper<Merchant> merchantWrapper = new LambdaQueryWrapper<>();
            merchantWrapper.eq(Merchant::getUserId, currentUserId);
            Merchant currentMerchant = merchantMapper.selectOne(merchantWrapper);
            if (currentMerchant != null && merchantIdToCheck != null && merchantIdToCheck.equals(currentMerchant.getId())) {
                allowed = true;
            }
        }

        if (!allowed) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }

        order.setStatus(statusDto.getStatus());

        Date now = new Date();
        OrderStatus orderStatus = OrderStatus.getByCode(statusDto.getStatus().toLowerCase())
                .orElseThrow(() -> new BusinessException(ResultStatus.ACCESS_DENIED));
        switch (orderStatus) {
            case PAID:
                if (order.getPaidAt() == null) order.setPaidAt(now);
                break;
            case COMPLETED:
                if (order.getCompletedAt() == null) order.setCompletedAt(now);
                break;
            case CANCELLED:
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

        String userRole = currentUser.getRole();
        Integer currentUserId = currentUser.getId();

        boolean allowed = "SUPER_ADMIN".equals(userRole) || "ADMIN".equals(userRole) || ("USER".equals(userRole) || "CUSTOMER".equals(userRole)) && order.getUserId().equals(currentUserId);

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

        if (!"MERCHANT".equals(currentUser.getRole())) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }

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
            // 🟢 使用存在的常量，含义可能最接近
            throw new BusinessException(ResultStatus.DATA_ALREADY_EXISTED, "Order has already been verified.");
        }


        OrderVerification verification = new OrderVerification();
        verification.setOrderId(orderId);
        verification.setVerifiedBy(currentMerchant.getId());
        verification.setLocation(verificationDto.getLocation());
        verification.setVerifiedAt(new Date());
        orderVerificationMapper.insert(verification);

        order.setStatus("completed");
        order.setCompletedAt(new Date());
        orderMapper.updateById(order);
    }

    @Override
    public OrderStatsDto getOrderStats(UserContext currentUser) {
        String userRole = currentUser.getRole();
        Integer currentUserId = currentUser.getId();

        UserRole role = UserRole.getByCode(userRole)
                .orElseThrow(() -> new BusinessException(ResultStatus.ACCESS_DENIED));

        switch (role) {
            case SUPER_ADMIN, ADMIN:
                return orderMapper.findAllOrderStats();
            case MERCHANT:
                LambdaQueryWrapper<Merchant> merchantWrapper = new LambdaQueryWrapper<>();
                merchantWrapper.eq(Merchant::getUserId, currentUserId);
                Merchant merchant = merchantMapper.selectOne(merchantWrapper);
                if (merchant == null) {
                    // 🟢 调用无参构造函数
                    return new OrderStatsDto();
                }
                return orderMapper.findOrderStatsByMerchantId(merchant.getId());
            case USER, CUSTOMER:
                return orderMapper.findOrderStatsByUserId(currentUserId);
            default:
                throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }
    }


    private OrderDetailResponse buildOrderDetailResponse(Order order) {
        if (order == null) throw new BusinessException(ResultStatus.FAIL, "order is null");

        OrderDetailResponse response = new OrderDetailResponse();
        OrderDto orderDto = convertToOrderDto(order);
        response.setOrder(orderDto);
        User user = userMapper.selectById(userContextHolder.getCurrentUser().getId());
        if (user != null) {
            OrderDetailResponse.UserInfo userInfo = new OrderDetailResponse.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setNickname(user.getNickname());
            userInfo.setPhone(user.getPhone());
            response.setUser(userInfo);
        }

        OrderDetailResponse.MerchantInfo merchantInfo = null;
        if ("single".equalsIgnoreCase(order.getOrderType()) && order.getBagId() != null) {
            MagicBag magicBag = magicBagMapper.selectById(order.getBagId());
            if (magicBag != null) {
                OrderDetailResponse.MagicBagInfo bagInfo = new OrderDetailResponse.MagicBagInfo();
                BeanUtils.copyProperties(magicBag, bagInfo);
                response.setMagicBag(bagInfo);

                Merchant merchant = merchantMapper.selectById(magicBag.getMerchantId());
                if (merchant != null) {
                    merchantInfo = new OrderDetailResponse.MerchantInfo();
                    BeanUtils.copyProperties(merchant, merchantInfo);
                }
            }
        } else if ("cart".equalsIgnoreCase(order.getOrderType()) && !CollectionUtils.isEmpty(orderDto.getOrderItems())) {
            OrderItemDto firstItem = orderDto.getOrderItems().isEmpty() ? null : orderDto.getOrderItems().getFirst();
            if (firstItem != null && firstItem.getMagicBagId() != null) {
                MagicBag firstBag = magicBagMapper.selectById(firstItem.getMagicBagId());
                if (firstBag != null) {
                    Merchant merchant = merchantMapper.selectById(firstBag.getMerchantId());
                    if (merchant != null) {
                        merchantInfo = new OrderDetailResponse.MerchantInfo();
                        BeanUtils.copyProperties(merchant, merchantInfo);
                    }
                }
            }
        }
        response.setMerchant(merchantInfo);

        QueryWrapper<OrderVerification> verificationWrapper = new QueryWrapper<>();
        verificationWrapper.eq("order_id", order.getId());
        List<OrderVerification> verifications = orderVerificationMapper.selectList(verificationWrapper);

        if (verifications != null) {
            List<OrderVerificationDto> verificationDtos = verifications.stream()
                    .map(this::convertToVerificationDto)
                    .toList();
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
        CartDto cart = cartService.getActiveCart(userId);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessException(ResultStatus.DATA_IS_WRONG, "Cart is empty or invalid.");
        }

        // 🟢 获取第一个商品的 MagicBag 以便获取取货时间
        CartItemDto firstCartItem = cart.getItems().getFirst();
        MagicBag firstBag = magicBagMapper.selectById(firstCartItem.getMagicbagId());
        if (firstBag == null) {
            throw new BusinessException(ResultStatus.DATA_IS_WRONG, "Could not find MagicBag details for the first cart item.");
        }
        LocalTime pickupStartTime = firstBag.getPickupStartTime();
        LocalTime pickupEndTime = firstBag.getPickupEndTime();
        // 🟢 将 LocalTime 转换为 java.util.Date (结合当前日期)
        LocalDate today = LocalDate.now();
        Date startDateTime = Date.from(pickupStartTime.atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        Date endDateTime = Date.from(pickupEndTime.atDate(today).atZone(ZoneId.systemDefault()).toInstant());

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CartItemDto item : cart.getItems()) {
            MagicBag bag = magicBagMapper.selectById(item.getMagicbagId());
            if (bag == null ||  !bag.isActive()) {
                throw new BusinessException(ResultStatus.FAIL, "Item " + item.getBagName() + " is not available.");
            }
            if (bag.getQuantity() < item.getQuantity()) {
                throw new BusinessException(ResultStatus.FAIL, "Insufficient stock for " + item.getBagName());
            }
            if (bag.getPrice() == null) {
                throw new BusinessException(ResultStatus.DATA_IS_WRONG, "Price not set for item " + item.getBagName());
            }
            totalPrice = totalPrice.add(BigDecimal.valueOf(bag.getPrice()).multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setOrderType("cart");
        // 🟢 设置 bag_id 为第一个商品的 ID，用于商家和用户查询订单
        order.setBagId(firstCartItem.getMagicbagId());
        // 🟢 设置 quantity 为购物车商品总数
        int totalQuantity = cart.getItems().stream()
                .mapToInt(CartItemDto::getQuantity)
                .sum();
        order.setQuantity(totalQuantity);
        order.setTotalPrice(totalPrice);
        order.setStatus("pending");
        order.setPickupCode(generatePickupCode());
        order.setCreatedAt(new Date());
        // 🟢 设置取货时间
        order.setPickupStartTime(startDateTime);
        order.setPickupEndTime(endDateTime);

        int insertedRows = orderMapper.insert(order);
        if (insertedRows == 0 || order.getId() == null) {
            throw new BusinessException(ResultStatus.FAIL, "Failed to create main order record.");
        }
        log.info("主订单创建成功, ID: {}", order.getId());


        for (CartItemDto item : cart.getItems()) {
            MagicBag bag = magicBagMapper.selectById(item.getMagicbagId());
            if (bag == null || bag.getPrice() == null) {
                log.error("在创建订单明细时未能找到 MagicBag 或其价格: ID {}", item.getMagicbagId());
                throw new BusinessException(ResultStatus.DATA_IS_WRONG, "Could not retrieve price for item " + item.getBagName());
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setMagicBagId(item.getMagicbagId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(BigDecimal.valueOf(bag.getPrice()));
            orderItem.setSubtotal(BigDecimal.valueOf(bag.getPrice()).multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItemMapper.insert(orderItem);

            int updatedStock = bag.getQuantity() - item.getQuantity();
            if (updatedStock < 0) {
                throw new BusinessException(ResultStatus.FAIL, "Concurrent stock update conflict for " + item.getBagName());
            }
            bag.setQuantity(updatedStock);
            magicBagMapper.updateById(bag);
            log.info("更新库存: MagicBag ID {}, 减少 {}, 剩余 {}", bag.getId(), item.getQuantity(), bag.getQuantity());
        }
        log.info("所有订单明细创建完成 for Order ID: {}", order.getId());

        cartService.clearCart(userId);
        log.info("购物车已清空 for User ID: {}", userId);


        Order createdOrder = orderMapper.selectById(order.getId());
        return convertToOrderDto(createdOrder);
    }


    private String generateOrderNo() {
        return "MB" + System.currentTimeMillis() + String.format("%04d", (random.nextInt(10000)));
    }


    private String generatePickupCode() {
        return String.format("%04d", (random.nextInt(10000)));
    }


    private OrderDto convertToOrderDto(Order order) {
        if (order == null) return null;
        OrderDto dto = new OrderDto();
        BeanUtils.copyProperties(order, dto);

        if ("cart".equalsIgnoreCase(order.getOrderType())) {
            List<OrderItem> orderItems = orderItemMapper.findByOrderId(order.getId());
            dto.setOrderItems(orderItems.stream()
                    .map(this::convertToOrderItemDto)
                    .filter(Objects::nonNull)
                    .toList());
        }

        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            dto.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        }

        if ("single".equalsIgnoreCase(order.getOrderType()) && order.getBagId() != null) {
            MagicBag bag = magicBagMapper.selectById(order.getBagId());
            if (bag != null) {
                dto.setBagTitle(bag.getTitle());
                Merchant merchant = merchantMapper.selectById(bag.getMerchantId());
                if (merchant != null) dto.setMerchantName(merchant.getName());
            }
        } else if ("cart".equalsIgnoreCase(order.getOrderType()) && !CollectionUtils.isEmpty(dto.getOrderItems())) {
            OrderItemDto firstItem = dto.getOrderItems().isEmpty() ? null : dto.getOrderItems().getFirst();
            if (firstItem != null && firstItem.getMagicBagId() != null) {
                MagicBag bag = magicBagMapper.selectById(firstItem.getMagicBagId());
                if (bag != null) {
                    dto.setBagTitle("Multiple Items");
                    Merchant merchant = merchantMapper.selectById(bag.getMerchantId());
                    if (merchant != null) dto.setMerchantName(merchant.getName() + " (and possibly others)");
                }
            }
        }

        return dto;
    }


    private OrderItemDto convertToOrderItemDto(OrderItem orderItem) {
        if (orderItem == null) return null;
        OrderItemDto dto = new OrderItemDto();
        BeanUtils.copyProperties(orderItem, dto);

        MagicBag magicBag = magicBagMapper.selectById(orderItem.getMagicBagId());
        if (magicBag != null) {
            dto.setMagicBagTitle(magicBag.getTitle());
            dto.setMagicBagImageUrl(magicBag.getImageUrl());
            dto.setMagicBagCategory(magicBag.getCategory());
        } else {
            log.warn("无法为 OrderItem ID {} 找到关联的 MagicBag ID {}", orderItem.getId(), orderItem.getMagicBagId());
            dto.setMagicBagTitle("Unknown Item");
        }
        return dto;
    }

}

