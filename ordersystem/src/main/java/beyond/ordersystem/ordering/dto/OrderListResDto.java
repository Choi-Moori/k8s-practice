package beyond.ordersystem.ordering.dto;

import beyond.ordersystem.ordering.domain.OrderDetail;
import beyond.ordersystem.ordering.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListResDto {
    private Long id;
    private String memberEmail;
    private OrderStatus orderStatus;
    private List<OrderDetailDto> orderDetailDtos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDetailDto {
        private Long id;
        private String productName;
        private Integer count;
    }

    public static OrderDetailDto fromEntity(OrderDetail orderDetail){
        return OrderDetailDto.builder()
                .id(orderDetail.getId())
                .productName(orderDetail.getProduct().getName())
                .count(orderDetail.getQuantity())
                .build();
    }
}
