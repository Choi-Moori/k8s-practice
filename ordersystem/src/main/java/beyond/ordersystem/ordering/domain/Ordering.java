package beyond.ordersystem.ordering.domain;

import beyond.ordersystem.member.domain.Member;
import beyond.ordersystem.ordering.dto.OrderListResDto;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Ordering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.ORDERED;

    @OneToMany(mappedBy = "ordering" , cascade = CascadeType.PERSIST)
//    빌더패턴에서도 ArrayList로 초기화 되도록 하는 설정.
    @Builder.Default
    private List<OrderDetail> orderDetails = new ArrayList<>();

    public OrderListResDto toEntity(){
        List<OrderListResDto.OrderDetailDto> OrderDetailDtos = new ArrayList<>();
        for(OrderDetail orderDetail : this.orderDetails){
            OrderDetailDtos.add(OrderListResDto.fromEntity(orderDetail));
        }
//        for(OrderDetail orderDetail : this.orderDetails){
//            OrderDetailDtos.add(orderDetail.fromEntity());
//        }
        return OrderListResDto.builder()
                .id(this.id)
                .memberEmail(this.member.getEmail())
                .orderStatus(this.orderStatus)
                .orderDetailDtos(OrderDetailDtos)
                .build();
    }

    public void cancelOrdering(OrderStatus orderStatus){
        this.orderStatus = orderStatus;
    }
}
