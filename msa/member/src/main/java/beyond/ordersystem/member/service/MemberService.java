package beyond.ordersystem.member.service;

import beyond.ordersystem.common.dto.CommonResDto;
import beyond.ordersystem.member.domain.Member;
import beyond.ordersystem.member.dto.MemberPasswordResetDto;
import beyond.ordersystem.member.dto.MemberSaveReqDto;
import beyond.ordersystem.member.dto.MemberLoginDto;
import beyond.ordersystem.member.dto.MemberResDto;
import beyond.ordersystem.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository){
        this.memberRepository = memberRepository;
    }

    public ResponseEntity<CommonResDto> memberCreate(MemberSaveReqDto dto){
        if(memberRepository.findByEmail(dto.getEmail()).isPresent())
            throw new IllegalArgumentException("중복된 이메일입니다.");

        Member member = dto.toEntity(passwordEncoder.encode(dto.getPassword()));
        memberRepository.save(member);
        CommonResDto memberResDto = new CommonResDto(HttpStatus.CREATED, "Member is successful created", member.getId());

        return new ResponseEntity<>(memberResDto, HttpStatus.CREATED);
    }

    public Page<MemberResDto> memberList(Pageable pageable){
        Page<Member> members = memberRepository.findAll(pageable);
        return members.map(Member::fromEntity);
    }

    public Member login(MemberLoginDto dto){
        Member member = memberRepository.findByEmail(dto.getEmail()).orElseThrow(()-> new EntityNotFoundException("일치하는 이메일이 존재하지 않습니다"));

        if(!passwordEncoder.matches(dto.getPassword(), member.getPassword()))
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");

        return member;
    }

    public void resetPassword(MemberPasswordResetDto dto){
        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(()-> new EntityNotFoundException("일치하는 이메일이 존재하지 않습니다."));
        if(!passwordEncoder.matches(dto.getAsIsPassword(), member.getPassword()))
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");

        member.resetPassword(passwordEncoder.encode(dto.getToBePassword()));
    }

    public MemberResDto myInfo(){
        Member member = memberRepository.findByEmail(
                SecurityContextHolder.getContext()
                                     .getAuthentication()
                                     .getName())
                .orElseThrow(()-> new EntityNotFoundException("not found"));

        return member.fromEntity();
    }
}
