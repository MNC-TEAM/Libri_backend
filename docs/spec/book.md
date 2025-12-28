# Book Spec (Thin)

## Policy
- 검색 키워드가 null/blank이면 빈 Slice를 반환하고 repository는 호출하지 않는다.
- 입력이 ISBN 형태면(하이픈/공백 포함 가능) ISBN을 정규화한 뒤 `findByIsbn()`으로 단건 조회를 시도한다.
    - 조회 성공: 1개짜리 Slice를 반환하고 hasNext=false
    - 조회 실패: 빈 Slice를 반환하고 hasNext=false
    - ISBN 분기에서는 `searchByKeyword()`를 호출하지 않는다.
- 입력이 ISBN이 아니면 텍스트 키워드를 공백 정규화한 뒤 `searchByKeyword(normalized, pageable)`로 통합 검색한다.
    - 결과 Slice의 hasNext는 repository 결과를 유지한다.

## Tests (Safety Belt)

### searchBooks
- [x] searchBooks_blankKeyword_returnsEmptySlice_andNoRepositoryCalls
    - keyword가 null/blank이면 빈 Slice를 반환하고 repository 호출을 하지 않는다.
- [x] searchBooks_isbnFound_returnsSingleSlice
    - ISBN 형태(하이픈/공백 포함)면 findByIsbn()으로 단건 조회를 시도하고 성공 시 1개 Slice를 반환한다.
- [x] searchBooks_isbnNotFound_returnsEmptySlice
    - ISBN 형태지만 DB에 없으면 빈 Slice를 반환한다.
- [x] searchBooks_notIsbn_callsSearchByKeyword_withNormalizedTextKeyword
    - ISBN이 아니면 searchByKeyword()로 통합 검색하고, 공백 정규화된 keyword로 호출한다.
