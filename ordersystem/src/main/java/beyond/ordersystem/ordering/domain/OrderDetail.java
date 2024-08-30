package beyond.ordersystem.ordering.domain;

import beyond.ordersystem.ordering.dto.OrderListResDto;
import beyond.ordersystem.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "ordering_id")
    private Ordering ordering;


    public OrderListResDto.OrderDetailDto fromEntity(){
        return OrderListResDto.OrderDetailDto.builder()
                .id(this.id)
                .productName(this.product.getName())
                .count(this.quantity)
                .build();
    }
}
