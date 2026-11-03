package edu.secure.lab.mapper;

import edu.secure.lab.domain.Loan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LoanMapper {

    /** [취약] ${} 문자열 치환 → SQL 삽입 */
    List<Loan> findByCustomerVuln(@Param("customerId") String customerId);

    /** [안전] #{} 바인딩 */
    List<Loan> findByCustomerSafe(@Param("customerId") String customerId);

    // =========================================================================
    // [실습 문제 1] 잔액 하한 조건을 추가한 매퍼 메서드 선언하기 (2/2)
    // =========================================================================
    // 목표: LoanMapper.xml에 작성한 <select id="findByCustomerMin"> 쿼리와 연결되는
    //       인터페이스 메서드를 선언하세요.
    //
    // 작성 힌트:
    // 1. 바로 위의 findByCustomerSafe 메서드 선언 모양을 참고하세요.
    // 2. 반환 타입은 List<Loan>, 메서드 이름은 findByCustomerMin 입니다.
    // 3. 파라미터는 2개를 받습니다:
    //    - @Param("customerId") String customerId
    //    - @Param("minBalance") long minBalance
    // TODO: 여기에 findByCustomerMin 메서드를 선언하세요.


    /** ORDER BY 는 바인딩할 수 없어 ${} 를 쓴다. 호출 전에 허용 목록 검증이 필수다. */
    List<Loan> findByCustomerSorted(@Param("customerId") String customerId, @Param("sortBy") String sortBy);

    /** [취약] 소유자 조건 없음 → IDOR */
    Loan findById(@Param("loanId") long loanId);

    /** [안전] 소유자 조건 포함 */
    Loan findByIdAndCustomer(@Param("loanId") long loanId, @Param("customerId") String customerId);

    Long selectBalance(@Param("loanId") long loanId);

    int updateBalance(@Param("loanId") long loanId, @Param("balance") long balance);

    /** [안전] 확인과 차감을 한 문장으로 처리 (원자적) */
    int repayConditional(@Param("loanId") long loanId, @Param("customerId") String customerId,
                         @Param("amount") long amount);
}
