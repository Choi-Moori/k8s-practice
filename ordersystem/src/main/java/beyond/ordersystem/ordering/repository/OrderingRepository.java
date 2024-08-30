package beyond.ordersystem.ordering.repository;

import beyond.ordersystem.member.domain.Member;
import beyond.ordersystem.ordering.domain.Ordering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderingRepository extends JpaRepository<Ordering,Long> {
    List<Ordering> findAllByMemberEmail(String email);

    List<Ordering> findAllByMember(Member member);
}

