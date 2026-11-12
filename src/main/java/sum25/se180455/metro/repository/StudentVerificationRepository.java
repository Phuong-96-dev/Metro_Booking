package sum25.se180455.metro.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sum25.se180455.metro.pojo.StudentVerification;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudentVerificationRepository extends JpaRepository<StudentVerification, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from StudentVerification v where v.id = :verificationId")
    Optional<StudentVerification> findByIdForUpdate(@Param("verificationId") Long verificationId);

    Optional<StudentVerification> findTopByMember_MemberIdOrderByCreatedAtDesc(Long memberId);

    boolean existsByMember_MemberIdAndStatus(Long memberId, String status);

    boolean existsByStudentCodeIgnoreCaseAndStatusIn(String studentCode, Collection<String> statuses);

    boolean existsByStudentCodeIgnoreCaseAndStatusInAndMember_MemberIdNot(
            String studentCode, Collection<String> statuses, Long memberId);

    List<StudentVerification> findByStatusOrderBySubmittedAtAsc(String status);

    @Query("""
            select v from StudentVerification v
            join v.member m
            where (:status is null or v.status = :status)
              and (:term is null
                   or lower(m.name) like lower(concat('%', :term, '%'))
                   or lower(m.email) like lower(concat('%', :term, '%'))
                   or lower(v.studentCode) like lower(concat('%', :term, '%')))
            order by v.submittedAt desc
            """)
    List<StudentVerification> search(@Param("status") String status, @Param("term") String term);
}
