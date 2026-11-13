package sum25.se180455.metro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.repository.MemberRepository;

import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberServiceImpl implements MemberService {
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Member register(Member member) {
        member.setEmail(member.getEmail().strip().toLowerCase(Locale.ROOT));
        if (memberRepository.findByEmail(member.getEmail()) != null) {
            throw new IllegalArgumentException("Email đã được đăng ký.");
        }
        member.setMemberId(null);
        member.setRole("MEMBER");
        member.setStatus("ACTIVE");
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        return memberRepository.saveAndFlush(member);
    }

    @Override
    public Member updateMember(Member member) {
        return memberRepository.save(member);
    }

    @Override
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId).orElse(null);
    }

    @Override
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @Override
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Override
    public Member updateStatus(Long memberId, String status) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with ID: " + memberId));
        member.setStatus(status);
        return memberRepository.save(member);
    }

    @Override
    public Member updateRole(Long memberId, String role) {
        if (!List.of("ADMIN", "MEMBER").contains(role)) {
            throw new IllegalArgumentException("Vai trò không hợp lệ.");
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with ID: " + memberId));
        member.setRole(role);
        return memberRepository.save(member);
    }

    @Override
    public int countMembersByRoleAndStatus(String role, String status) {
        return memberRepository.countByRoleAndStatus(role, status);
    }


}
