package monochrome.libri.member.controller;

import jakarta.validation.Valid;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.member.dto.request.MemberCreateRequestDto;
import monochrome.libri.member.dto.response.MemberResponseDto;
import monochrome.libri.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 회원 탈퇴 (현재 로그인한 사용자 기준)
     * DELETE /api/v1/auth/me
     */
    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(Long memberId) {
        memberService.withdraw(memberId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
