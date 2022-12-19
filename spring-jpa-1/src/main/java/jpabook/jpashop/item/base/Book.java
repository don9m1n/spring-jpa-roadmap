package jpabook.jpashop.item.base;

import jpabook.jpashop.item.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@Getter
@Setter
@DiscriminatorValue("B")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Book extends Item {
    private String author;
    private String isbn;
}
