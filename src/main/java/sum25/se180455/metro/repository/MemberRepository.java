package sum25.se180455.metro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sum25.se180455.metro.pojo.Member;

import jakarta.persistence.LockModeType;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmailAndPassword(String email, String password);
    Member findByEmail(String email);
    // count members by role and status
    int countByRoleAndStatus(String role, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.memberId = :memberId")
    Optional<Member> findByIdForUpdate(@Param("memberId") Long memberId);
}
