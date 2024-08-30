package beyond.ordersystem.member.controller;

import beyond.ordersystem.common.auth.JwtTokenProvider;
import beyond.ordersystem.common.dto.CommonErrorDto;
import beyond.ordersystem.common.dto.CommonResDto;
import beyond.ordersystem.member.domain.Member;
import beyond.ordersystem.member.dto.*;
import beyond.ordersystem.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/member")
public class MemberController {

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    @Qualifier("2")
    private final RedisTemplate<String, Object> redisTemplate;

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public MemberController(@Qualifier("2") RedisTemplate redisTemplate,
                            MemberService memberService,
                            JwtTokenProvider jwtTokenProvider){
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/create")
    public ResponseEntity<CommonResDto> memberCreate(@Valid @RequestBody MemberSaveReqDto dto){
        return memberService.memberCreate(dto);
    }

//    admin만 회원목록 전체 조회 가능.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> memberList(Pageable pageable){
        Page<MemberResDto> dtos = memberService.memberList(pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "members are found", dtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

//    본인은 본인회원정보만 조회가능
//    /member/myinfo. MemberResDto
    @GetMapping("/myinfo")
    public ResponseEntity<?> myInfo(){
        MemberResDto dto = memberService.myInfo();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "member is found", dto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @PostMapping("/doLogin")
    @ResponseBody
    public ResponseEntity<?> doLogin(@RequestBody MemberLoginDto dto){
//        email, password가 일치하는지 검증
        Member member = memberService.login(dto);
//        일치할 경우 accessToken 생성
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), member.getRole().toString());

//        redis에 email과 rt를 key:value로 하여 저장.
        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 240, TimeUnit.HOURS); // 240시간
//        생성된 토큰을 CommonResDto에 담아 사용자에게 return
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("token", jwtToken);
        loginInfo.put("refreshToken", refreshToken);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "member login is successful", loginInfo);
        return new ResponseEntity<>(commonResDto,HttpStatus.OK);
    }

    @PatchMapping("/reset-password")
    @ResponseBody
    public ResponseEntity<?> doResetPassword(@RequestBody MemberPasswordResetDto dto){
        memberService.resetPassword(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "password reset successful", "ok");

        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    @ResponseBody
    public ResponseEntity<?> generateNewAccessToken(@RequestBody MemberRefreshDto dto){
        String rt = dto.getRefreshToken();
        Claims claims = null;
        try {
//            코드를 통해 rt 검증
            claims = Jwts.parser().setSigningKey(secretKeyRt).parseClaimsJws(rt).getBody();
        }catch (Exception e){
            return new ResponseEntity<>(
                    new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), "invalid refresh token"),
                    HttpStatus.BAD_REQUEST);
        }

        String email = claims.getSubject();
        String role = claims.get("role").toString();

//        redis를 조회하여 rt 추가 검증.
//        opsForValue는 Object객체이다. String 에서 toString() 으로 받을 때
//        만약 값이 삭제되어 없다면 null을 받아 toString() 에서 에러가 난다.
        Object obj = redisTemplate.opsForValue().get(email);
        if(obj == null || !obj.toString().equals(rt)) {
            return new ResponseEntity<>(
                    new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), "invalid refresh token"),
                    HttpStatus.BAD_REQUEST);
        }
        String newAt = jwtTokenProvider.createToken(email, role);
        //        생성된 토큰을 CommonResDto에 담아 사용자에게 return

        Map<String, Object> info = new HashMap<>();
        info.put("token", newAt);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "at is renewed", info);
        return new ResponseEntity<>(commonResDto,HttpStatus.OK);
    }
}
