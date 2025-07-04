package sum25.se180455.metro.service;

import sum25.se180455.metro.pojo.Member;

public interface MemberService {

    public Member login(String email, String password);

    public Member register(Member member);

    public Member updateMember(Member member);

    public Member findById(Long memberId);
}
