# Decisions Log

> 이 문서는 프로젝트 전반의 “정책/설계 결정”과 그 이유를 기록한다.  
> 각 spec(auth.md 등)에는 **최신 정책만** 유지하고, 변경 이유/배경은 여기 남긴다.

---

## Auth

### 2025-12-29
- Change: MEMBER_WITHDRAWN → INVALID_LOGIN
- Why: 보안(계정 상태/존재 여부 추측 방지)

