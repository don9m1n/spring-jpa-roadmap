package jpabook.jpashop.order.service;

import jpabook.jpashop.base.data.Address;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.item.base.Book;
import jpabook.jpashop.item.entity.Item;
import jpabook.jpashop.member.entity.Member;
import jpabook.jpashop.order.entity.Order;
import jpabook.jpashop.order.entity.OrderStatus;
import jpabook.jpashop.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @PersistenceContext EntityManager em;
    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    @Test
    void 상품주문() throws Exception {
        Member member = createMember();
        Item item = createBook("SPRING JPA", 10000, 10);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        Order order = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.ORDER, order.getStatus(), "상품 주문시 상태는 ORDER");
        assertEquals(1, order.getOrderItems().size(), "주문한 상품 종류 수가 정확해야한다.");
        assertEquals(10000 * 2, order.getTotalPrice(), "주문 가격은 가격 * 수량이다.");
        assertEquals(8, item.getStockQuantity(), "주문 수량만큼 재고가 줄어야 한다.");
    }

    @Test
    void 상품주문_재고수량초과() throws Exception {
        Member member = createMember();
        Item item = createBook("CentOS9", 20000, 20);

        int orderCount = 21;
        assertThrows(NotEnoughStockException.class, () -> {
            orderService.order(member.getId(), item.getId(), orderCount);
        }, "재고 수량 예외가 발생해야 한다.");
    }
    
    @Test
    void 주문취소() throws Exception {
        Member member = createMember();
        Item item = createBook("CentOS9", 20000, 10);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        // 주문 취소 -> 재고 증가
        orderService.cancelOrder(orderId);

        Order order = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.CANCEL, order.getStatus(), "주문 취소시 상태는 CANCEL 이다.");
        assertEquals(10, item.getStockQuantity(), "주문이 취소된 상품은 그만큼 재고가 증가해야 한다.");
    }

    private Member createMember() {
        Member member = Member.builder()
                .name("동민")
                .address(new Address("서울", "동대문구", "휘경동"))
                .build();
        em.persist(member);
        return member;
    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = Book.builder()
                .name(name)
                .price(price)
                .stockQuantity(stockQuantity)
                .build();
        em.persist(book);
        return book;
    }
}