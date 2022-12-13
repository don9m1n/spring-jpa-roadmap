package jpabook.jpashop.delivery.entity;

import jpabook.jpashop.base.data.Address;
import jpabook.jpashop.base.entity.BaseEntity;
import jpabook.jpashop.order.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Delivery extends BaseEntity {

    @OneToOne(mappedBy = "delivery", fetch = LAZY)
    private Order order;

    @Embedded
    private Address address;

    @Enumerated(STRING)
    private DeliveryStatus status;

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setAddressAndStatus(Address address, DeliveryStatus status) {
        this.address = address;
        this.status = status;
    }
}
