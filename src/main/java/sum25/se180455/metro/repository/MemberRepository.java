package sum25.se180455.metro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sum25.se180455.metro.pojo.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("select m from Member m where lower(trim(m.email)) = lower(trim(:email))")
    Member findByEmail(@Param("email") String email);
    // count members by role and status
    int countByRoleAndStatus(String role, String status);
}
