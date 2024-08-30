package beyond.ordersystem.product.domain;

import beyond.ordersystem.common.domain.BaseTimeEntity;
import beyond.ordersystem.ordering.domain.OrderDetail;
import beyond.ordersystem.product.dto.ProductResDto;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private Integer price;
    private Integer stockQuantity;
    private String imagePath;

    @OneToMany(mappedBy = "product" , cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetailList;

    public ProductResDto toEntity(){
        return ProductResDto.builder()
                .id(this.id)
                .name(this.name)
                .category(this.category)
                .price(this.price)
                .stockQuantity(this.stockQuantity)
                .imagePath(this.imagePath)
                .build();
    }

    public void updateImagePath(String imagePath){
        this.imagePath = imagePath;
    }

    public void updateStockQuantity(int quantity){
        this.stockQuantity = this.stockQuantity-quantity;
    }

    public void cancelOrdering(int quantity){
        this.stockQuantity += quantity;
    }
}
