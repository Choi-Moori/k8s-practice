package beyond.ordersystem.common.domain;

import lombok.*;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Address {
    private String city;
    private String street;
    private String zipcode;
}
