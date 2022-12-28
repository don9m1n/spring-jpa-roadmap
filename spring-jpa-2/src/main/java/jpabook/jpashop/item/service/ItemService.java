package jpabook.jpashop.item.service;

import jpabook.jpashop.item.base.Book;
import jpabook.jpashop.item.controller.BookForm;
import jpabook.jpashop.item.entity.Item;
import jpabook.jpashop.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    @Transactional
    public void changeItem(BookForm form) {
        Book book = (Book) itemRepository.findOne(form.getId());
        book.changeBook(form);
    }

    public Item findOne(Long id) {
        return itemRepository.findOne(id);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }
}
