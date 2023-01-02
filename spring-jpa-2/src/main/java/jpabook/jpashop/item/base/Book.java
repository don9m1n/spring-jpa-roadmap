package jpabook.jpashop.item.base;

import jpabook.jpashop.item.controller.BookForm;
import jpabook.jpashop.item.entity.Item;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@Getter
@Setter(AccessLevel.PRIVATE)
@DiscriminatorValue("B")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Book extends Item {
    private String author;
    private String isbn;

    public static Book create(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);

        return book;
    }

    // 정적 팩토리 메서드
    public static Book createBook(BookForm form) {
        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        return book;
    }

    // 변경 메서드
    public void changeBook(BookForm form) {
        super.changeItem(form.getName(), form.getPrice(), form.getStockQuantity());
        this.author = form.getAuthor();
        this.isbn = form.getIsbn();
    }
}
