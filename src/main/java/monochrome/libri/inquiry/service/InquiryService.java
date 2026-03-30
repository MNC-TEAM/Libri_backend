package monochrome.libri.inquiry.service;

import monochrome.libri.inquiry.dto.request.InquiryAnswerRequestDto;
import monochrome.libri.inquiry.dto.request.InquiryCreateRequestDto;
import monochrome.libri.inquiry.dto.request.InquiryStatusUpdateRequestDto;
import monochrome.libri.inquiry.dto.response.InquiryDetailResponseDto;
import monochrome.libri.inquiry.dto.response.InquiryListResponseDto;
import org.springframework.data.domain.Pageable;

public interface InquiryService {
    InquiryDetailResponseDto createInquiry(long memberId, InquiryCreateRequestDto request);
    InquiryListResponseDto getMyInquiries(long memberId, Pageable pageable);
    InquiryDetailResponseDto getInquiry(long inquiryId, long memberId);
    InquiryListResponseDto getAllInquiries(long memberId, Pageable pageable);
    InquiryDetailResponseDto answerInquiry(long memberId, long inquiryId, InquiryAnswerRequestDto request);
    InquiryDetailResponseDto updateInquiryStatus(long memberId, long inquiryId, InquiryStatusUpdateRequestDto request);
}
