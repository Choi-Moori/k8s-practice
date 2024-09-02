package beyond.ordersystem.common.configs;

import beyond.ordersystem.common.auth.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity // 이 코드가 security 관련 코드이다.
@EnableGlobalMethodSecurity(prePostEnabled = true) // pre : 사전검증, post : 사후검증 - 사전 검증 하겠다.
public class SecurityConfigs {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;


//    filter 레벨에서 발생하는 예외는 Spring 밖에서 막히기 때문에 디버깅이 어렵다.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity
                .csrf().disable()
                /*
                    CORS : Cross Origin Resource Sharing => 다른 도메인에서 서버로 호출하는 것을 금지.
                */
                .cors().and() // CORS 활성화
                .httpBasic().disable()
                .authorizeRequests()
                    // member/create 페이지 제외, / : 홈 화면 제외, doLogin 화면 제외
                    .antMatchers("/member/create", "/", "/member/doLogin", "/member/refresh-token","/product/list", "/member/reset-password")
                    .permitAll()
                .anyRequest().authenticated()
                .and()
//                세션로그인이 아닌 stateless한 token을 사용하겠다라는 의미
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
//                사용자로부터 받아온 토근이 정상인지 아닌지 검증하는 코드
                /*
                로그인시 사용자는 서버로부터 토큰을 발급받고,
                매요청마다 해당 토근을 http header 넣어 요청
                아래 코드는 사용자로부터 받아온 토큰이 정상인지 아닌지를 검증하는 코드.
                토큰 :
                    1) 헤더, 페이로드, 서명부 3가지 부분이 . 으로 이루어져 있음.
                    2) header, payload는 인코딩. 서명부는 암호화.
                    3) 서명부 : (인코딩된 헤더 + 인코딩된 페이로드 + 비밀키) 암호화
                    -> SHA256알고리즘을 통해 암호화.
                 */
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
