package jpabook.jpashop.api;

import jpabook.jpashop.base.data.Address;
import jpabook.jpashop.order.entity.Order;
import jpabook.jpashop.order.entity.OrderSearch;
import jpabook.jpashop.order.entity.OrderStatus;
import jpabook.jpashop.order.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        return all;
    }

    /**
     * v2 버전처럼 api 스펙에 맞는 dto 객체를 만들면 최적화해서 개발이 가능하다.
     * v2의 문제점 - lazy loading으로 인한 N + 1 문제 발생
     * 지연 로딩은 영속성 컨텍스트부터 접근한다.
     * N + 1 : 첫 번째 쿼리의 결과로 추가 쿼리가 N번 실행되는 문제
     * 페치 조인으로 해결 가능!!
     */
    @GetMapping("/v2/simple-orders")
    public Result<List<SimpleOrderDto>> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> simpleOrders = orders.stream()
                .map(SimpleOrderDto::new) // Method Reference (람다 생략)
                .collect(Collectors.toList());

        return new Result<>(simpleOrders.size(), simpleOrders);
    }

    /**
     * API 필드 이름은 dto의 필드 이름과 동일하다.
     * 엔티티로 반환할 때도 필드 이름을 변경할 수 있지만 그러지 말자..
    */
    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name; // 주문자
        private LocalDateTime orderDate; // 주문 날짜
        private OrderStatus orderStatus; // 주문 상태
        private Address address;

        /**
         * DTO 클래스안에서 엔티티를 파라미터로 받는 건 괜찮다.
         * 결국 외부로 노출만 하지 않는다면 상관없음!
         * */
        public SimpleOrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
        }
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        private T data;
    }
}
