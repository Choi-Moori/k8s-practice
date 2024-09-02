package beyond.ordersystem.ordering.domain;

import beyond.ordersystem.ordering.dto.OrderListResDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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

    private Long productId;

    @ManyToOne
    @JoinColumn(name = "ordering_id")
    private Ordering ordering;


    public OrderListResDto.OrderDetailDto fromEntity(){
        return OrderListResDto.OrderDetailDto.builder()
                .id(this.id)
                .count(this.quantity)
                .build();
    }
}
