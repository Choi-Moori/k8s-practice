package beyond.ordersystem.member.domain;

import beyond.ordersystem.common.domain.Address;
import beyond.ordersystem.common.domain.BaseTimeEntity;
import beyond.ordersystem.member.dto.MemberPasswordResetDto;
import beyond.ordersystem.member.dto.MemberResDto;
import beyond.ordersystem.ordering.domain.Ordering;
import beyond.ordersystem.ordering.service.OrderingService;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    private String password;
    @Embedded
    private Address address;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ordering> orderings;

    public MemberResDto fromEntity(){
        return MemberResDto.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .orderCount(this.orderings.size())
                .address(this.address)
                .build();
    }

    public void resetPassword(String toBePassword){
        this.password = toBePassword;
    }
}
