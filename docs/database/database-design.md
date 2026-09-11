## 공통 DB 정책

- DB 기본 문자셋은 `utf8mb4`, 기본 collation은 `utf8mb4_0900_ai_ci`를 사용합니다.
- 문자열 정규화는 애플리케이션에서 수행합니다.
- DB에는 이 문서의 제약조건 항목에 명시된 CHECK만 적용하며, 명시되지 않은 Enum 및 BOOLEAN CHECK를 임의로 추가하지 않습니다.
- Enum 허용값은 DB CHECK 정의 여부와 관계없이 애플리케이션에서도 검증합니다.
- 사용자 소유 리소스 사이의 동일 사용자 여부는 DB 복합 FK가 아니라 Service의 소유권 검증으로 보장합니다. 예를 들어 `receipt.user_id`와 `category.user_id`, `category_rule.user_id`와 `category.user_id`의 일치 여부를 Service에서 검증합니다.

---

## 사용자(회사) → user_company

| 한글명 | 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- | --- |
| 사용자 아이디 | `user_id` | BIGINT | PK, AI | 사용자 식별자 |
| SNS 사용자 식별자 | `sns_id` | VARCHAR(100) | NN, UK1 | OAuth 제공자가 발급한 사용자 식별자 |
| OAuth 제공자 | `oauth_provider` | VARCHAR(20) | NN, UK1 | 사용자가 가입 또는 로그인에 사용한 OAuth 제공자 |
| 상호 | `company_name` | VARCHAR(100) | NULL | 회사 이름 |
| 사업자등록번호 | `business_number` | CHAR(12) | NULL | 하이픈을 포함한 사업자등록번호. 예: `111-11-11111` |
| 회사 업종 | `business_type` | VARCHAR(100) | NULL | 회사의 업종 |
| 전화번호 | `phone_number` | CHAR(11) | NULL | 하이픈을 제외한 전화번호 |
| 월 지출 예산 | `monthly_expense_budget` | INT | NULL | 사용자가 설정한 월 지출 예산 |
| 영수증 등록 가능 시작일 | `receipt_start_date` | DATE | NULL | 사용자가 영수증을 등록할 수 있는 가장 이른 날짜 |
| 최근 로그인 지역 정보 | `last_login_region` | VARCHAR(100) | NULL | 사용자가 최근 로그인한 지역 정보 |
| 서비스 이용 상태 | `user_status` | VARCHAR(50) | NN | 사용자의 회원가입 완료 여부 및 서비스 이용 상태 |

### `oauth_provider` 값

| `oauth_provider` 값 | 설명 |
| --- | --- |
| `KAKAO` | 카카오 OAuth로 가입 또는 로그인 |
| `NAVER` | 네이버 OAuth로 가입 또는 로그인 |
| `GOOGLE` | 구글 OAuth로 가입 또는 로그인 |

### `user_status` 값

| `user_status` 값 | 설명 |
| --- | --- |
| `SIGNUP_REQUIRED` | SNS 인증은 완료했지만 추가 회원정보 입력이 필요한 상태 |
| `ACTIVE` | 추가 회원정보 등록을 완료하여 서비스를 이용할 수 있는 상태 |

### 제약조건

| 제약조건 | 대상 컬럼 | 설명 |
| --- | --- | --- |
| PK | `user_id` | 사용자 식별 |
| UK1 | `oauth_provider`, `sns_id` | 동일 OAuth 제공자 안에서 같은 SNS 사용자가 중복 등록되는 것을 방지 |

---

## 로그인 이력 테이블 → login_history

| 한글명 | 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- | --- |
| 로그인 이력 아이디 | `login_history_id` | BIGINT | PK, AI | 로그인 이력 식별자 |
| 사용자 아이디 | `user_id` | BIGINT | FK, NN | 로그인한 사용자 |
| IP 주소 | `ip_address` | VARCHAR(45) | NN | 로그인 요청의 IP 주소 |
| 로그인 지역 | `login_region` | VARCHAR(100) | NULL | IP 주소를 기준으로 추정한 지역. 지역을 확인할 수 없는 경우 NULL |
| 로그인 일시 | `login_at` | DATETIME | NN | 사용자가 로그인한 날짜와 시간 |

### 제약조건

| 제약조건 | 대상 컬럼 | 설명 |
| --- | --- | --- |
| PK | `login_history_id` | 로그인 이력 식별 |
| FK | `user_id` | `user_company.user_id` 참조 |

---

## OCR 옵션 → ocr_option

| 한글명 | 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- | --- |
| OCR 옵션 아이디 | `ocr_option_id` | BIGINT | PK, AI | OCR 옵션 식별자 |
| 사용자 아이디 | `user_id` | BIGINT | FK, NN, UK | OCR 옵션을 설정한 사용자 |
| 자동 증빙 분류 여부 | `auto_classification_enabled` | BOOLEAN | NN, DEFAULT FALSE | OCR 결과를 기준으로 증빙을 자동 분류할지 여부 |
| OCR 신뢰도 임계값 | `ocr_confidence_threshold` | INT | NN, DEFAULT 80 | OCR 결과가 검수 대상으로 판단되는 신뢰도 기준값 |

### 제약조건

| 제약조건 | 대상 컬럼 | 설명 |
| --- | --- | --- |
| PK | `ocr_option_id` | OCR 옵션 식별 |
| FK | `user_id` | `user_company.user_id` 참조 |
| UK | `user_id` | 사용자 한 명당 하나의 OCR 옵션만 등록할 수 있도록 제한 |
| CHECK | `auto_classification_enabled` | `TRUE` 또는 `FALSE`만 저장 가능 |
| CHECK | `ocr_confidence_threshold` | 0 이상 100 이하의 값만 저장 가능 |

---

## OCR 옵션별 문서 유형 → ocr_option_document_type

| 한글명 | 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- | --- |
| OCR 옵션 문서 유형 아이디 | `ocr_option_document_type_id` | BIGINT | PK, AI | OCR 옵션과 문서 유형의 연결 정보 식별자 |
| OCR 옵션 아이디 | `ocr_option_id` | BIGINT | FK, NN, UK1 | 문서 유형이 설정된 OCR 옵션 |
| 문서 유형 아이디 | `document_type_id` | BIGINT | FK, NN, UK1 | OCR 옵션에 설정된 문서 유형 |

### 제약조건

| 구분 | 대상 컬럼 | 설명 |
| --- | --- | --- |
| PK | `ocr_option_document_type_id` | OCR 옵션과 문서 유형의 연결 정보를 식별하는 기본키 |
| FK | `ocr_option_id` | `ocr_option.ocr_option_id` 참조 |
| FK | `document_type_id` | `document_type.document_type_id` 참조 |
| UK1 | `ocr_option_id`, `document_type_id` | 하나의 OCR 옵션에 동일한 문서 유형이 중복 설정되지 않도록 하는 복합 유니크 제약조건 |

---

## 문서 유형 → document_type

| 한글명 | 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- | --- |
| 문서 유형 아이디 | `document_type_id` | BIGINT | PK, AI | 문서 유형 식별자 |
| 문서 유형명 | `document_type_name` | VARCHAR(100) | NN, UK | 사용자에게 표시되는 문서 유형명 |

### 허용 문서 유형

| `document_type_name` |
| --- |
| 영수증 |
| 세금계산서 |
| 카드전표 |
| 현금영수증 |
| 기타 증빙 |

### 제약조건

| 구분 | 대상 컬럼 | 설명 |
| --- | --- | --- |
| PK | `document_type_id` | 문서 유형을 식별하는 기본키 |
| UK | `document_type_name` | 동일한 문서 유형명이 중복 저장되지 않도록 제한 |

---

## 카테고리 테이블 → category

| 한글명 | 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- | --- |
| 카테고리 아이디 | `category_id` | BIGINT | PK, AI | 카테고리 식별자 |
| 사용자 아이디 | `user_id` | BIGINT | FK, NN, UK1 | 카테고리를 소유한 사용자 |
| 카테고리명 | `category_name` | VARCHAR(100) | NN, UK1 | 사용자에게 표시되는 카테고리명 |
| 카테고리 활성화 여부 | `is_active` | BOOLEAN | NN, DEFAULT TRUE | 카테고리 사용 여부 |

### 기본 제공 카테고리

| `category_name` |
| --- |
| 광고비 |
| 택배비 |
| 소모품 |
| 외주비 |
| 교통비 |
| 식대 |
| 사무용품 |
| 서비스 이용료 |
| 통신비 |
| 기타 |

### 제약조건

| 구분 | 대상 컬럼 | 설명 |
| --- | --- | --- |
| PK | `category_id` | 카테고리를 식별하는 기본키 |
| FK | `user_id` | `user_company.user_id` 참조 |
| UK1 | `user_id`, `category_name` | 한 사용자가 동일한 이름의 카테고리를 중복 생성하지 못하도록 하는 복합 유니크 제약조건 |

---

## 영수증 → receipt

| 한글명 | 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- | --- |
| 영수증 아이디 | `receipt_id` | BIGINT | PK, AI | 증빙 식별자 |
| 사용자 아이디 | `user_id` | BIGINT | FK, NN, UK1 | 증빙을 소유한 사용자 |
| 카테고리 아이디 | `category_id` | BIGINT | FK, NULL | 증빙에 적용된 카테고리. 미분류 상태이면 NULL |
| 거래일시 | `transaction_at` | DATETIME | NULL | 증빙에서 추출하거나 사용자가 입력한 거래일시 |
| 거래처명 | `merchant_name` | VARCHAR(200) | NULL | 거래처 또는 가맹점 이름 |
| 사업자등록번호 | `merchant_business_number` | VARCHAR(20) | NULL | 거래처 사업자등록번호. 검수 전의 잘못된 OCR 값도 저장할 수 있도록 가변 길이 사용 |
| 문서 유형 아이디 | `document_type_id` | BIGINT | FK, NULL | 증빙의 문서 유형 |
| 공급가액 | `supply_amount` | BIGINT | NULL | 전체 증빙 항목의 세전 합계 |
| 부가세 | `vat_amount` | BIGINT | NULL | 증빙에 표시된 부가세 |
| 총액 | `total_amount` | BIGINT | NULL | 최종 결제 금액 |
| 결제수단 | `payment_method` | VARCHAR(30) | NULL | 카드, 현금 등 결제수단 |
| 승인번호 또는 증빙번호 | `approval_number` | VARCHAR(100) | NULL | 승인번호 또는 증빙번호 중 추출된 값을 저장 |
| 이미지 URL | `image_url` | VARCHAR(500) | NULL | OCR 및 검수 화면에서 사용하는 이미지 URL |
| 원본 파일 해시값 | `original_file_hash` | CHAR(64) | NULL, UK1 | 원본 파일의 SHA-256 해시값. 직접 입력은 NULL |
| 검수 상태 | `review_status` | VARCHAR(20) | NN | 증빙의 현재 검수 상태 |
| 증빙 등록 방식 | `registration_type` | VARCHAR(20) | NN | OCR 등록 또는 직접 입력 |
| OCR 신뢰도 | `ocr_confidence` | INT | NULL | 실제 OCR 결과 신뢰도. 0 이상 100 이하 |
| 추천 카테고리명 | `recommended_category_name` | VARCHAR(100) | NULL | GPT가 추천한 카테고리명 |
| 카테고리 추천 신뢰도 | `category_confidence` | INT | NULL | GPT 카테고리 추천 신뢰도. 0 이상 100 이하 |
| 중복 의심 점수 | `duplicate_suspicion_score` | INT | NN, DEFAULT 0 | 기존 증빙과 중복일 가능성. 0 이상 100 이하 |
| 업무 관련성 위험 점수 | `company_relevance_risk_score` | INT | NULL | 사용자가 업무 관련성을 확인해야 하는 정도. 0 이상 100 이하 |
| 메모 | `memo` | TEXT | NULL | 사용자가 작성한 증빙 메모 |
| 영수증 생성 일시 | `created_at` | DATETIME | NN | 증빙 생성 일시 |
| 영수증 최종 처리 일시 | `finalized_at` | DATETIME | NULL | 증빙 상태가 `CONFIRMED`, `DUPLICATE` 또는 `EXCLUDED`로 최종 처리된 날짜와 시간 |
| 증빙 삭제 일시 | `deleted_at` | DATETIME | NULL | 증빙이 논리 삭제된 일시삭제되지 않은 경우 `NULL` |

### `review_status` 허용값

| 저장값 | 의미 | 설명 |
| --- | --- | --- |
| `CONFIRMED` | `확정` | 모든 검증을 통과했거나 사용자가 검수를 완료한 정상 증빙 |
| `REVIEW_REQUIRED` | `검수` | 하나 이상의 검수 사유가 있어 사용자 확인이 필요한 증빙 |
| `UNCLASSIFIED` | `미분류` | 검수 사유는 없지만 카테고리가 적용되지 않은(`category_id = NULL`) 증빙 |
| `DUPLICATE` | `중복` | 사용자가 검수 후 실제 중복으로 확정한 증빙 |
| `EXCLUDED` | `제외` | 사용자가 회사 업무와 관련 없는 지출이라고 확인하여 정산 대상에서 제외 |

### 최종 상태 정책

- `CONFIRMED`, `DUPLICATE`, `EXCLUDED`는 최종 상태로 취급합니다.
- 최종 상태로 전환할 때 `finalized_at`을 기록합니다.
- 최종 상태로 전환된 증빙은 `memo`를 제외한 주요 거래정보를 수정할 수 없습니다.

### `registration_type` 허용값

| 저장값 | 설명 |
| --- | --- |
| `OCR` | 증빙 파일을 업로드하고 OCR 분석을 통해 등록 |
| `MANUAL` | 원본 증빙 파일이 없는 누락 내역을 사용자가 수기로 등록 |

### 중복 의심 점수 산정 기준

| 점수 | 산정 조건 |
| --- | --- |
| 95 | 승인번호 또는 증빙번호가 동일하고 거래일자, 총금액, 거래처 중 하나 이상이 추가로 일치 |
| 90 | 거래처명, 거래일자, 총금액, 사업자등록번호가 모두 일치 |
| 85 | 승인번호 또는 증빙번호가 동일하지만 95점의 추가 일치 조건은 충족하지 않음 |
| 75 | 거래처명, 거래일자, 총금액이 모두 일치하지만 90점 조건은 충족하지 않음 |
| 0 | 중복으로 판단할 근거가 없음 |

### 제약조건

| 구분 | 대상 컬럼 | 설명 |
| --- | --- | --- |
| PK | `receipt_id` | 영수증을 식별하는 기본키 |
| FK | `user_id` | `user_company.user_id` 참조 |
| FK | `category_id` | `category.category_id` 참조 |
| FK | `document_type_id` | `document_type.document_type_id` 참조 |
| UK1 | `user_id`, `original_file_hash` | 논리 삭제 여부와 관계없이 동일 사용자의 동일 원본 파일 중복 등록 방지 |
| CHECK | `review_status` | `CONFIRMED`, `REVIEW_REQUIRED`, `UNCLASSIFIED`, `DUPLICATE`, `EXCLUDED`만 허용 |
| CHECK | `registration_type` | `OCR`, `MANUAL`만 허용 |
| CHECK | `ocr_confidence` | NULL 또는 0 이상 100 이하 |
| CHECK | `category_confidence` | NULL 또는 0 이상 100 이하 |
| CHECK | `duplicate_suspicion_score` | 0 이상 100 이하 |
| CHECK | `company_relevance_risk_score` | NULL 또는 0 이상 100 이하 |

---

## 영수증 품목 → receipt_item

| 한글명 | 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- | --- |
| 영수증 품목 아이디 | `receipt_item_id` | BIGINT | PK, AI | 영수증 품목 식별자 |
| 영수증 아이디 | `receipt_id` | BIGINT | FK, NN | 품목이 포함된 영수증 |
| 품목명 | `item_name` | VARCHAR(200) | NN | OCR에서 추출하거나 사용자가 입력·수정한 품목명 |
| 수량 | `quantity` | INT | NULL | 증빙에 표시된 품목 수량. 추출할 수 없는 경우 NULL |
| 품목 금액 | `item_amount` | INT | NULL | 증빙에 표시된 해당 품목의 금액. 확인할 수 없는 경우 NULL |

### 제약조건

| 구분 | 대상 컬럼 | 설명 |
| --- | --- | --- |
| PK | `receipt_item_id` | 영수증 품목을 식별하는 기본키 |
| FK | `receipt_id` | `receipt.receipt_id` 참조 |

---

## 증빙 검수 사유 → receipt_review_reason

- 하나의 증빙에서 발생한 **현재 검수 사유**를 저장합니다.
- 동일한 증빙에 서로 다른 검수 사유가 동시에 존재할 수 있습니다.
- 동일한 증빙에 동일한 `reason_type`의 검수 사유는 하나만 유지합니다.
- 증빙 값 수정 후 재검증하는 경우 과거 검수 결과를 누적하지 않고 현재 검수 결과로 갱신합니다.
- 중복 검수는 중복 판단 방식과 관계없이 `DUPLICATE` 하나의 검수 사유 유형으로 관리합니다.

| 한글명 | 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- | --- |
| 증빙 검수 사유 아이디 | `receipt_review_reason_id` | BIGINT | PK, AI | 증빙 검수 사유 식별자 |
| 영수증 아이디 | `receipt_id` | BIGINT | FK, NN, UK1 | 현재 검수가 필요한 영수증 |
| 검수 사유 유형 | `reason_type` | VARCHAR(50) | NN, UK1 | 발생한 검수 사유 유형 |
| 검수 사유 메시지 | `reason_message` | TEXT | NN | 사용자에게 보여줄 검수 사유 |
| 검수 해결 여부 | `is_resolved` | BOOLEAN | NN, DEFAULT FALSE | 검수 사유가 해결되었는지 여부 |
| 검수 사유 생성 날짜 | `created_at` | DATETIME | NN | 검수 사유가 생성된 날짜 |

### 검사 사유 유형 (`reason_type` )

| 검수 사유 유형 | 설명 |
| --- | --- |
| `LOW_OCR_CONFIDENCE` | 전체 OCR 신뢰도가 사용자의 기준값보다 낮음 |
| `REQUIRED_FIELD_MISSING` | 필수값 누락 |
| `INVALID_TRANSACTION_DATETIME` | 거래일시 형식 오류 |
| `INVALID_BUSINESS_NUMBER` | 사업자등록번호 형식 오류 |
| `AMOUNT_MISMATCH` | 공급가액 + 부가세와 총금액 불일치 |
| `DUPLICATE` | 기존 증빙과의 중복 가능성에 대한 검수 필요 |
| `COMPANY_RELEVANCE` | 회사 업무 관련성 검수 필요 |
| `MANUAL_NO_ORIGINAL_EVIDENCE` | 원본 증빙이 없어 수기로 등록한 증빙 |
| `MANUAL_OCR_UNAVAILABLE` | 원본 증빙은 있으나 OCR 처리가 어려워 수기로 등록한 증빙 |
| `MANUAL_ALTERNATIVE_EVIDENCE` | 원본 대신 대체 증빙을 첨부하여 수기로 등록한 증빙 |

### 제약조건

| 제약조건 | 대상 컬럼 | 설명 |
| --- | --- | --- |
| PK | `receipt_review_reason_id` | 검수 사유 식별 |
| FK | `receipt_id` | `receipt.receipt_id` 참조 |
| UNIQUE | `receipt_id`, `reason_type` | 동일한 영수증에 동일한 검수 사유 유형이 중복 생성되는 것을 방지 |

---

## 증빙 검수 문제 필드 → receipt_review_field

- 검수 사유와 관련하여 **검수 화면에서 사용자가 확인해야 하는 증빙 필드**를 저장합니다.
- 프론트엔드는 이 정보를 이용하여 해당 필드 옆에 아이콘을 표시하고, 사용자가 **확인이 필요한 필드를 한눈에 인지할 수 있도록 합니다.**
- 하나의 검수 사유와 관련된 필드가 여러 개라면 필드마다 하나의 행을 생성합니다.
- 저장된 필드의 의미는 연결된 `receipt_review_reason.reason_type`에 따라 결정됩니다.
    - 필수값 누락 → 실제 누락된 필드
    - 형식 오류 → 형식이 잘못된 필드
    - 금액 불일치 → 금액 검증에 관련된 필드
    - 중복 의심 → 기존 증빙과 실제 일치한 필드
- 특정 증빙 필드와 직접 연결하기 어려운 검수 사유는 행을 생성하지 않습니다.

| 한글명 | 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- | --- |
| 증빙 검수 사유 아이디 | `receipt_review_reason_id` | BIGINT | PK, FK, NN | 문제가 발생한 검수 사유 |
| 검수 관련 필드명 | `field_name` | VARCHAR(50) | PK, NN | 누락, 형식 오류 또는 값 불일치가 발생한 필드,

### 제약조건

| 제약조건 | 대상 컬럼 | 설명 |
| --- | --- | --- |
| 복합 PK | `receipt_review_reason_id`, `field_name` | 동일 검수 사유에 같은 필드가 중복 저장되는 것을 방지 |
| FK | `receipt_review_reason_id` | `receipt_review_reason.receipt_review_reason_id` 참조 |

### 검수 사유 유형별 필드 저장 기준

| 검수 사유 유형 | 필드 저장 여부 | 저장되는 필드 | 저장 의미 |
| --- | --- | --- | --- |
| `LOW_OCR_CONFIDENCE` | 저장하지 않음 | - | 전체 OCR 결과의 신뢰도에 대한 검수 사유이므로 특정 필드와 연결하지 않음 |
| `REQUIRED_FIELD_MISSING` | 저장 | 실제 누락된 필드 | OCR 결과에서 필수값을 추출하지 못한 필드를 저장 |
| `INVALID_TRANSACTION_DATETIME` | 저장 | `transaction_at` | 거래일시 값의 형식이 유효하지 않아 확인이 필요한 필드 |
| `INVALID_BUSINESS_NUMBER` | 저장 | `merchant_business_number` | 사업자등록번호 형식이 유효하지 않아 확인이 필요한 필드 |
| `AMOUNT_MISMATCH` | 저장 | `supply_amount`, `vat_amount`, `total_amount` | 공급가액 + 부가세와 총금액의 일치 여부를 사용자가 확인해야 하는 필드 |
| `DUPLICATE`  | 저장 | 기존 증빙과 실제 일치한 필드 | 중복 판단의 근거가 된 일치 필드 |
| `COMPANY_RELEVANCE` | 저장하지 않음 | - | 여러 정보를 종합하여 판단하므로 특정 필드 하나와 연결하지 않음 |
| `MANUAL_NO_ORIGINAL_EVIDENCE` | 저장하지 않음 | - | 특정 필드 오류가 아니라 원본 증빙 없이 수기로 등록했다는 검수 사유 |
| `MANUAL_OCR_UNAVAILABLE` | 저장하지 않음 | - | 특정 필드 오류가 아니라 OCR 처리 불가로 수기 등록했다는 검수 사유 |
| `MANUAL_ALTERNATIVE_EVIDENCE` | 저장하지 않음 | - | 특정 필드 오류가 아니라 대체 증빙을 이용한 수기 등록이라는 검수 사유 |

### 추가 설명

#### `REQUIRED_FIELD_MISSING`

필수값이 여러 개 누락된 경우에는 **실제로 누락된 필드마다 각각 하나의 행을 생성합니다.**

예를 들어 거래처명과 총금액이 누락된 경우:

- `merchant_name`
- `total_amount`

을 각각 저장합니다.

#### `AMOUNT_MISMATCH`

공급가액, 부가세, 총금액은 세 값을 함께 비교하여 검증하므로 특정 필드 하나만 문제라고 판단하지 않습니다.

따라서 금액 불일치가 발생하면 다음 필드를 모두 저장합니다.

- `supply_amount`
- `vat_amount`
- `total_amount`

#### `DUPLICATE`

기존 증빙과 **실제로 일치하여 중복 점수 산정에 사용된 필드**를 저장합니다.

- **95점**
    - 승인번호 또는 증빙번호가 동일하고, 거래일시·총금액·거래처명 중 하나 이상이 추가로 일치한 경우
    - `approval_number`
    - 추가로 실제 일치한 필드
    - 예: 승인번호와 총금액이 일치한 경우
        - `approval_number`
        - `total_amount`
- **90점**
    - 거래처명 + 거래일시 + 총금액 + 사업자등록번호가 모두 동일한 경우
    - `merchant_name`
    - `transaction_at`
    - `total_amount`
    - `merchant_business_number`
- **85점**
    - 승인번호 또는 증빙번호만 동일한 경우
    - `approval_number`
- **75점**
    - 거래처명 + 거래일시 + 총금액이 모두 동일한 경우
    - `merchant_name`
    - `transaction_at`
    - `total_amount`

#### `LOW_OCR_CONFIDENCE`

특정 필드의 문제가 아니라 **OCR 결과 전체의 신뢰도**가 기준보다 낮아 발생하는 검수 사유이므로 `receipt_review_field`에 행을 생성하지 않습니다.

#### `COMPANY_RELEVANCE`

회사 업종, 거래처, 품목명, 거래일시, 금액 등을 종합하여 업무 관련성 검수 필요 여부를 판단하므로 특정 증빙 필드 하나와 연결하지 않습니다. 따라서 `receipt_review_field`에 행을 생성하지 않습니다.

#### `MANUAL_NO_ORIGINAL_EVIDENCE`

원본 증빙 없이 사용자가 직접 입력하여 등록한 증빙이므로 특정 증빙 필드 하나의 문제가 아닙니다. 따라서 `receipt_review_field`에 행을 생성하지 않습니다.

#### `MANUAL_OCR_UNAVAILABLE`

원본 증빙은 존재하지만 OCR 처리가 어려워 사용자가 직접 입력하여 등록한 증빙이므로 특정 증빙 필드 하나의 문제가 아닙니다. 따라서 `receipt_review_field`에 행을 생성하지 않습니다.

#### `MANUAL_ALTERNATIVE_EVIDENCE`

원본 증빙 대신 대체 증빙을 첨부하여 사용자가 직접 입력한 증빙이므로 특정 증빙 필드 하나의 문제가 아닙니다. 따라서 `receipt_review_field`에 행을 생성하지 않습니다.

---

## 증빙 중복 검수 상세 → receipt_duplicate_review

- `DUPLICATE` 검수 사유에 필요한 **중복 검수 전용 상세 정보**를 저장합니다.
- 하나의 `DUPLICATE` 검수 사유에는 하나의 중복 검수 상세만 존재합니다.
- 중복 조건을 만족하는 기존 증빙 중 가장 높은 중복 의심 점수를 가진 증빙 1건을 비교 대상으로 저장합니다.
- 동일한 최고 점수를 가진 기존 증빙이 여러 건 존재하는 경우에는 가장 최근에 등록된 기존 증빙 1건을 선택합니다.
- 사용자가 증빙 정보를 수정하여 중복 검증을 다시 수행한 경우 과거 중복 분석 결과를 누적하지 않고 현재 비교 대상으로 갱신합니다.
- 재검증 결과 중복 조건이 더 이상 성립하지 않는 경우 해당 중복 검수 상세을 삭제합니다.
- 사용자가 `다른 증빙입니다`로 중복 검수를 완료한 경우에는 중복 검증 결과 자체가 사라진 것이 아니므로 중복 검수 상세는 유지하고, `receipt_review_reason.is_resolved`만 해결 상태로 변경합니다.
- 연결된 `receipt_review_reason.reason_type`이 `DUPLICATE`인지 애플리케이션 도메인 로직에서 검증합니다.
- 검수 대상 증빙과 `related_receipt_id`가 같은 증빙이 아닌지 애플리케이션 도메인 로직에서 검증합니다.
- 검수 대상 증빙과 `related_receipt_id`가 같은 사용자의 증빙인지 애플리케이션 도메인 로직에서 검증합니다.

| 한글명 | 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- | --- |
| 증빙 검수 사유 아이디 | `receipt_review_reason_id` | BIGINT | PK, FK, NN | `DUPLICATE` 검수 사유이자 중복 검수 상세 식별자 |
| 관련 기존 영수증 아이디 | `related_receipt_id` | BIGINT | FK, NN | 가장 높은 중복 의심 점수를 만든 기존 영수증 |

### 제약조건

| 제약조건 | 대상 컬럼 | 설명 |
| --- | --- | --- |
| PK, FK | `receipt_review_reason_id` | `receipt_review_reason.receipt_review_reason_id`를 참조하며, 하나의 `DUPLICATE` 검수 사유마다 중복 검수 상세은 최대 1건만 저장할 수 있도록 합니다. |
| FK | `related_receipt_id` | `receipt.receipt_id` 참조 |

---

## 증빙 변경 이력 테이블 → receipt_change_log

- 사용자가 증빙의 주요 거래정보를 수정한 경우 변경 전·후 값을 필드 단위로 저장합니다.
- 최초 등록 값을 별도의 변경 이력으로 저장하지 않으며, 사용자가 증빙 정보를 처음 수정할 때 현재 저장된 값을 `before_value`에 저장합니다. 값이 실제로 변경되지 않은 경우에는 변경 이력을 생성하지 않습니다.
- `CONFIRMED`, `DUPLICATE` 또는 `EXCLUDED`로 최종 처리된 증빙은 주요 정보를 수정할 수 없습니다. 단, 최종 처리 이후에도 `memo`는 수정할 수 있으며, 메모는 증빙 검증 결과에 영향을 주지 않는 부가정보이므로 변경 이력을 기록하지 않습니다.
- `after_value`는 NOT NULL을 유지하며 NULL을 허용하지 않습니다.

| 한글명 | 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- | --- |
| 증빙 변경 이력 아이디 | `receipt_change_log_id` | BIGINT | PK, AI | 증빙 변경 이력 식별자 |
| 영수증 아이디 | `receipt_id` | BIGINT | FK, NN | 변경된 영수증 |
| 변경된 필드명 | `field_name` | VARCHAR(50) | NN | 값이 변경된 `receipt` 테이블의 필드명 |
| 변경 전 값 | `before_value` | VARCHAR(500) | NULL | 필드가 변경되기 전의 값을 문자열로 저장 |
| 변경 후 값 | `after_value` | VARCHAR(500) | NN | 필드가 변경된 후의 값을 문자열로 저장 |
| 변경 발생 일시 | `created_at` | DATETIME | NN | 해당 필드의 값이 변경된 날짜와 시간 |

### 변경 이력 기록 대상

| `field_name` | 기록 내용 |
| --- | --- |
| `category_id` | 사용자가 카테고리를 변경한 경우 |
| `transaction_at` | 거래일시 변경 |
| `merchant_name` | 거래처명 변경 |
| `merchant_business_number` | 사업자등록번호 변경 |
| `document_type_id` | 문서 유형 변경 |
| `supply_amount` | 공급가액 변경 |
| `vat_amount` | 부가세 변경 |
| `total_amount` | 총액 변경 |
| `payment_method` | 결제수단 변경 |
| `approval_number` | 승인번호 또는 증빙번호 변경 |

### 제약조건

| 구분 | 대상 | 설명 |
| --- | --- | --- |
| PK | `receipt_change_log_id` | 증빙 변경 이력을 식별하는 기본키 |
| FK | `receipt_id` | `receipt.receipt_id` 참조 |

---

## 카테고리 분류 규칙 테이블 → category_rule

| 한글명 | 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- | --- |
| 카테고리 분류 규칙 아이디 | `category_rule_id` | BIGINT | PK, AI | 카테고리 분류 규칙 식별자 |
| 사용자 아이디 | `user_id` | BIGINT | FK, NN, UK1 | 카테고리 분류 규칙을 소유한 사용자 |
| 거래처명 | `merchant_name` | VARCHAR(200) | NN, UK1 | 카테고리 자동 분류 기준으로 사용하는 정규화된 거래처명 |
| 카테고리 아이디 | `category_id` | BIGINT | FK, NULL | 해당 거래처에 적용할 카테고리. 
연결된 카테고리가 비활성화되어 재지정이 필요한 경우 NULL |
| 규칙 생성 일시 | `created_at` | DATETIME | NN | 카테고리 분류 규칙이 생성된 날짜와 시간 |
| 규칙 수정 일시 | `updated_at` | DATETIME | NN | 적용 카테고리가 마지막으로 수정된 날짜와 시간 |

### 제약조건

| 구분 | 대상 컬럼 | 설명 |
| --- | --- | --- |
| PK | `category_rule_id` | 카테고리 분류 규칙을 식별하는 기본키 |
| FK | `user_id` | `user_company.user_id` 참조 |
| FK | `category_id` | `category.category_id` 참조 |
| UK1 | `user_id`, `merchant_name` | 한 사용자가 동일한 거래처에 여러 분류 규칙을 생성하지 못하도록 제한 |

---

## 작업 활동 로그 - workspace_activity_log

| 한글명 | 컬럼명 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- | --- |
| 작업 활동 로그 아이디 | `activity_log_id` | BIGINT | PK, AI | 작업 활동 로그 식별자 |
| 사용자 아이디 | `user_id` | BIGINT | FK, NN | 해당 작업 활동이 발생한 사용자 |
| 작업 활동 유형 | `activity_type` | VARCHAR(50) | NN | 발생한 작업의 유형 |
| 작업 활동 내용 | `activity_message` | VARCHAR(255) | NN | 사용자에게 보여줄 작업 활동 내용 |
| 작업 활동 발생 일시 | `created_at` | DATETIME | NN | 작업 활동이 발생한 날짜와 시간 |

### 제약조건

| 구분 | 대상 컬럼 | 설명 |
| --- | --- | --- |
| PK | `activity_log_id` | 작업 활동 로그를 식별하는 기본키 |
| FK | `user_id` | `user_company.user_id` 참조 |

### 작업 활동 유형 (`activity_type`)

| activity_type | 저장 시점 | 저장할 `activity_message` 예시 |
| --- | --- | --- |
| `RECEIPT_REGISTERED` | 검수 사유가 없고 카테고리까지 지정되어 처음부터 `확정` 상태로 등록 | `스타벅스 증빙 등록 및 확정` |
| `MANUAL_RECEIPT_REGISTERED` | `MANUAL` 방식으로 증빙 등록 | `스타벅스 수기 증빙 등록` |
| `RECEIPT_VALIDATION_REQUIRED` | OCR 결과에 검수가 필요한 항목이 존재 | `스타벅스 OCR 결과 확인 필요` |
| `UNCLASSIFIED_DETECTED` | 검수 사유는 없지만 카테고리가 지정되지 않음 | `스타벅스 카테고리 지정 필요` |
| `DUPLICATE_DETECTED` | 중복 의심 검수 사유 발생 | `스타벅스 중복 가능성 확인 필요` |
| `COMPANY_RELEVANCE_DETECTED` | 회사 관련성 검수 사유 발생 | `스타벅스 업무 관련성 확인 필요` |
| `RECEIPT_CONFIRMED` | 사용자가 검수 후 정상 증빙으로 확정 | `스타벅스 증빙 확정` |
| `DUPLICATE_CONFIRMED` | 사용자가 실제 중복 증빙으로 처리 | `스타벅스 중복 증빙 처리` |
| `RECEIPT_EXCLUDED` | 사용자가 업무 무관 증빙으로 판단하여 제외 | `스타벅스 정산 대상 제외` |
| `TAX_FILE_CREATED` | 세무사 제출 파일 생성 완료 | `세무사 제출 ZIP 생성` |
| `SETTINGS_UPDATED` | 주요 설정 변경 | `OCR 자동 분류 설정 변경` |
| `RECEIPT_DELETED` | 사용자가 증빙을 논리적으로 삭제 | `스타벅스 증빙 삭제` |

#### 거래처명에 따른 메시지 저장 기준

증빙과 관련된 작업 활동 메시지는 작업이 발생한 시점의 거래처명을 기준으로 생성합니다.

- 거래처명이 존재하는 경우에는 메시지 앞에 거래처명을 포함합니다.
- 거래처명이 `null`이거나 공백인 경우에는 거래처명을 생략하고 작업 내용만 저장합니다.

| 작업 내용 | 거래처명 존재 | 거래처명 없음 |
| --- | --- | --- |
| OCR 결과 확인 | `스타벅스 OCR 결과 확인 필요` | `OCR 결과 확인 필요` |
| 카테고리 지정 | `스타벅스 카테고리 지정 필요` | `카테고리 지정 필요` |
| 중복 가능성 확인 | `스타벅스 중복 가능성 확인 필요` | `중복 가능성 확인 필요` |
| 증빙 삭제 | `스타벅스 증빙 삭제` | `증빙 삭제` |
