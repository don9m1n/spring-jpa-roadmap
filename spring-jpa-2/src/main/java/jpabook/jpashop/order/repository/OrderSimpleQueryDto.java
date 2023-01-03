package jpabook.jpashop.order.repository;

import jpabook.jpashop.base.data.Address;
import jpabook.jpashop.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OrderSimpleQueryDto {
    private Long orderId;
    private String name; // 주문자
    private LocalDateTime orderDate; // 주문 날짜
    private OrderStatus orderStatus; // 주문 상태
    private Address address;
}
