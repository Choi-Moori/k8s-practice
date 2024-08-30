package beyond.ordersystem.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

// service 와 같은 레벨의 싱클톤 class로 만들어지기 위해 COmponent
@Component
public class JwtTokenProvider {

    @Value("${jwt.secretKey}")
    private String secretKey;
    @Value("${jwt.expiration}")
    private int expiration;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;
    @Value("${jwt.expirationRt}")
    private int expirationRt;

    public String createToken(String email, String role){
//        claims 는 사용자 정보(페이로드 정보)
//        setSubject는 이메일이나 Id를 의미
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)   // 생성시간
                .setExpiration(new Date(now.getTime() + expiration*60*1000L))   // 만료시간 : 30분
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
        return token;
    }
    public String createRefreshToken(String email, String role){
//        claims 는 사용자 정보(페이로드 정보)
//        setSubject는 이메일이나 Id를 의미
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)   // 생성시간
                .setExpiration(new Date(now.getTime() + expirationRt*60*1000L))   // 만료시간 : 30분
                .signWith(SignatureAlgorithm.HS256, secretKeyRt)
                .compact();

        return token;
    }
}
