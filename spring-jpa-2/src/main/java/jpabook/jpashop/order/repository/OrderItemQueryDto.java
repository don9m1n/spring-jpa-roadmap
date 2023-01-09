package jpabook.jpashop.order.repository;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderItemQueryDto {

    private Long orderId;
    private String itemName; // 상품 이름
    private int orderPrice; // 상품 가격
    private int count; // 수량
}
