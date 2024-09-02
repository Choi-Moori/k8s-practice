package beyond.ordersystem.member.domain;

import beyond.ordersystem.common.domain.Address;
import beyond.ordersystem.common.domain.BaseTimeEntity;
import beyond.ordersystem.member.dto.MemberPasswordResetDto;
import beyond.ordersystem.member.dto.MemberResDto;
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

    public MemberResDto fromEntity(){
        return MemberResDto.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .address(this.address)
                .build();
    }

    public void resetPassword(String toBePassword){
        this.password = toBePassword;
    }
}
