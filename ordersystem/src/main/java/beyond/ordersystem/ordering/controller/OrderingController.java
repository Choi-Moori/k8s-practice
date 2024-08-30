package beyond.ordersystem.ordering.controller;

import beyond.ordersystem.common.dto.CommonResDto;
import beyond.ordersystem.ordering.domain.Ordering;
import beyond.ordersystem.ordering.dto.OrderSaveReqDto;
import beyond.ordersystem.ordering.dto.OrderListResDto;
import beyond.ordersystem.ordering.service.OrderingService;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderingController {

    private final OrderingService orderingService;

    @Autowired
    public OrderingController(OrderingService orderingService) {
        this.orderingService = orderingService;
    }

    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> orderCreate(@RequestBody List<OrderSaveReqDto> dto){
        Ordering ordering = orderingService.orderCreate(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "정상완료", ordering.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<?> orderList(){
        List<OrderListResDto> orderList = orderingService.orderList();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "조회 완료", orderList);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

//    내 주문만 볼 수 있는 myOrders : order/myorders
    @GetMapping("/myorders")
    @ResponseBody
    public ResponseEntity<?> myOrders(){
        List<OrderListResDto> dto = orderingService.myOrder();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "조회 완료", dto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

//    admin사용자가 주문 취소 : /order/{id}/cancel -> orderstatus만 변경
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> orderCancel(@PathVariable Long id){
        Ordering ordering = orderingService.orderCancel(id);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "취소 완료", ordering);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}