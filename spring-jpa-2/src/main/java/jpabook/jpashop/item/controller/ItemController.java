package jpabook.jpashop.item.controller;

import jpabook.jpashop.item.base.Book;
import jpabook.jpashop.item.entity.Item;
import jpabook.jpashop.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", itemService.findItems());
        return "items/item-list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    @PostMapping("/new")
    public String create(BookForm form) {
        Book book = Book.createBook(form);
        itemService.saveItem(book);
        return "redirect:/";
    }

    @GetMapping("/{id}/edit")
    public String updateForm(@PathVariable("id") Long id, Model model) {
        Book book = (Book) itemService.findOne(id);

        BookForm form = BookForm.createBookForm(book);
        model.addAttribute("form", form);

        return "items/updateItemForm";
    }

    @PostMapping("/{id}/edit")
    public String update(@ModelAttribute("form") BookForm form) {
        itemService.changeItem(form);

        return "redirect:/";
    }
}
