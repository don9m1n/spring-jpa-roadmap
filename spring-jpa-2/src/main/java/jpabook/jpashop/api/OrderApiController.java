package jpabook.jpashop.api;

import jpabook.jpashop.base.data.Address;
import jpabook.jpashop.order.entity.Order;
import jpabook.jpashop.order.entity.OrderSearch;
import jpabook.jpashop.order.entity.OrderStatus;
import jpabook.jpashop.order.repository.OrderRepository;
import jpabook.jpashop.orderitem.entity.OrderItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll();
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    @GetMapping("/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        return orders.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Fetch Join으로 필요한 엔티티 한 번에 가져오는 방식
     * Fetch Join : 프록시 객체가 아닌 진짜 엔티티에 값을 담아서 가져오는 방식
     */
    @GetMapping("/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();

        List<OrderDto> result = new ArrayList<>();
        for (Order order : orders) {
            OrderDto dto = new OrderDto(order);
            result.add(dto);
        }

        return result;
    }

    @GetMapping("/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {

        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        List<OrderDto> result = new ArrayList<>();
        for (Order order : orders) {
            OrderDto dto = new OrderDto(order);
            result.add(dto);
        }

        return result;
    }

    @Data
    static class OrderDto {
        private Long orderId;
        private String name; // 주문자
        private LocalDateTime orderDate; // 주문 날짜
        private OrderStatus orderStatus; // 주문 상태
        private Address address; // 값 타입은 노출 ㄱㅊ
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(Collectors.toList());
        }
    }

    @Data
    static class OrderItemDto {

        private String itemName; // 상품 이름
        private int orderPrice; // 상품 가격
        private int count; // 수량

        public OrderItemDto(OrderItem orderItem) {
            this.itemName = orderItem.getItem().getName(); // ??
            this.orderPrice = orderItem.getOrderPrice();
            this.count = orderItem.getCount();
        }
    }
}
