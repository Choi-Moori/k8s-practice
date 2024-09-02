package beyond.ordersystem.ordering.repository;

import beyond.ordersystem.ordering.domain.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    List<?> findAllByOrderingId(Long id);
}
