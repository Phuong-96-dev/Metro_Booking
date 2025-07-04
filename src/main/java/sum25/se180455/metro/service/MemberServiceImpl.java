package sum25.se180455.metro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.repository.MemberRepository;

@Service
public class MemberServiceImpl implements MemberService {
    @Autowired
    MemberRepository memberRepository;

    @Override
    public Member login(String email, String password) {
    return  memberRepository.findByEmailAndPassword(email, password);
    }

    @Override
    public Member register(Member member) {
        return memberRepository.save(member);
    }

    @Override
    public Member updateMember(Member member) {
        return memberRepository.save(member);
    }

    @Override
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId).orElse(null);
    }

}
