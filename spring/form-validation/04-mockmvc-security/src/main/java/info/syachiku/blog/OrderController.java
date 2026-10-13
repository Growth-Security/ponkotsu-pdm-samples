package info.syachiku.blog;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public String create(
            @Valid @ModelAttribute("orderForm") OrderForm form,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "orders/input";
        }
        orderService.create(new OrderRequest(form.getCustomerId(), form.getQuantity()));
        return "redirect:/orders";
    }

    // 比較実験用：BindingResultを外した場合を同じ入力で確認する。
    @PostMapping("/orders/no-binding")
    public String createWithoutBindingResult(
            @Valid @ModelAttribute("orderForm") OrderForm form) {
        orderService.create(new OrderRequest(form.getCustomerId(), form.getQuantity()));
        return "redirect:/orders";
    }
}
