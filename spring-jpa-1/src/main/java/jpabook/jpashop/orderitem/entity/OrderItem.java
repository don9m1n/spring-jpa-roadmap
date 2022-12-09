package jpabook.jpashop.orderitem.entity;

import jpabook.jpashop.base.entity.BaseEntity;
import jpabook.jpashop.item.entity.Item;
import jpabook.jpashop.order.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_id")
    private Item item;
    private int orderPrice; // 주문 당시 가격 (가격은 변경 가능성이 있기 때문에!)
    private int count; // 주문 당시 수량
}
