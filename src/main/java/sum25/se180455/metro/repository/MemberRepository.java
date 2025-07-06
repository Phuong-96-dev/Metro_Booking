package sum25.se180455.metro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sum25.se180455.metro.pojo.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmailAndPassword(String email, String password);
    Member findByEmail(String email);
    // count members by role and status
    int countByRoleAndStatus(String role, String status);
}
