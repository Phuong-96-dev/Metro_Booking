package sum25.se180455.metro.service;

import sum25.se180455.metro.pojo.Member;

import java.util.List;

public interface MemberService {

    public Member login(String email, String password);

    public Member register(Member member);

    public Member updateMember(Member member);

    public Member findById(Long memberId);

    // Manage member

    public List<Member> findAll();

    public Member findByEmail(String email);

    public Member updateStatus(Long memberId, String status);

    public Member updateRole(Long memberId, String role);

    public int countMembersByRoleAndStatus(String role, String status);
}
