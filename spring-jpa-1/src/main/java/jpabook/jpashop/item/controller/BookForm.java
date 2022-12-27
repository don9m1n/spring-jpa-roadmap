package jpabook.jpashop.item.controller;

import jpabook.jpashop.item.base.Book;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookForm{
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    private String author;
    private String isbn;

    public static BookForm createBookForm(Book book) {
        BookForm form = new BookForm();
        form.setId(book.getId());
        form.setName(book.getName());
        form.setPrice(book.getPrice());
        form.setStockQuantity(book.getStockQuantity());
        form.setAuthor(book.getAuthor());
        form.setIsbn(book.getIsbn());

        return form;
    }
}
