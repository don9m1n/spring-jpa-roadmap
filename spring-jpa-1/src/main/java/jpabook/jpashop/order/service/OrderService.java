package jpabook.jpashop.order.service;

import jpabook.jpashop.base.data.Address;
import jpabook.jpashop.delivery.entity.Delivery;
import jpabook.jpashop.delivery.entity.DeliveryStatus;
import jpabook.jpashop.item.entity.Item;
import jpabook.jpashop.item.repository.ItemRepository;
import jpabook.jpashop.member.entity.Member;
import jpabook.jpashop.member.repository.MemberRepository;
import jpabook.jpashop.order.entity.Order;
import jpabook.jpashop.order.repository.OrderRepository;
import jpabook.jpashop.orderitem.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    // 주문
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);

        Delivery delivery = new Delivery();
        delivery.setAddressAndStatus(member.getAddress(), DeliveryStatus.READY);

        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);
        Order order = Order.createOrder(member, delivery, orderItem);

        orderRepository.save(order);

        return order.getId();
    }

    // 주문 취소
    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository.findOne(id);
        order.cancel();
    }

    // 주문 검색
    /**
    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findAll(orderSearch);
    }
     */
}
