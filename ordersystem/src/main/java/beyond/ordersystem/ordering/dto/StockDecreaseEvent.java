package beyond.ordersystem.ordering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDecreaseEvent {
    private Long productId;
    private Integer productCount;
}
