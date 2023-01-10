package jpabook.jpashop.order.repository;

import jpabook.jpashop.order.entity.Order;
import jpabook.jpashop.orderitem.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders();
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });

        return result;
    }

    private List<OrderItemQueryDto> findOrderItems(Long id) {
        return em.createQuery("select new jpabook.jpashop.order.repository.OrderItemQueryDto" +
                "(oi.order.id, oi.item.name, oi.orderPrice, oi.count)" +
                "from OrderItem oi " +
                "join oi.item i " +
                "where oi.order.id =: id", OrderItemQueryDto.class)
                .setParameter("id", id)
                .getResultList();
    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery("select new jpabook.jpashop.order.repository.OrderQueryDto" +
                "(o.id, m.name, o.orderDate, o.status, d.address)" +
                "from Order o " +
                "join o.member m " +
                "join o.delivery d ", OrderQueryDto.class)
                .getResultList();
    }
}
