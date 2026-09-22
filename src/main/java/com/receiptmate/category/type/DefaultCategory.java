package com.receiptmate.category.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DefaultCategory {
    ADVERTISING("광고비"),
    DELIVERY("택배비"),
    CONSUMABLES("소모품"),
    OUTSOURCING("외주비"),
    TRANSPORTATION("교통비"),
    MEALS("식대"),
    OFFICE_SUPPLIES("사무용품"),
    SERVICE_FEE("서비스 이용료"),
    COMMUNICATION("통신비"),
    OTHER("기타");

    private final String categoryName;
}
