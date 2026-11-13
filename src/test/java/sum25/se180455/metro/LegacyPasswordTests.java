package sum25.se180455.metro;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.repository.MemberRepository;
import sum25.se180455.metro.security.MemberAuthenticationProvider;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LegacyPasswordTests {
    @Test void explicitlyEnabledLegacyLoginUpgradesPasswordToBcrypt() {
        MemberRepository repository = mock(MemberRepository.class);
        Member member = new Member(); member.setMemberId(1L); member.setEmail("old@example.com");
        member.setPassword("old-password"); member.setRole("MEMBER"); member.setStatus("active");
        when(repository.findByEmail("old@example.com")).thenReturn(member);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
        var provider = new MemberAuthenticationProvider(repository, encoder, true);
        assertThat(provider.authenticate(UsernamePasswordAuthenticationToken.unauthenticated("old@example.com", "old-password")).isAuthenticated()).isTrue();
        assertThat(encoder.matches("old-password", member.getPassword())).isTrue();
        verify(repository).saveAndFlush(member);
    }
}
