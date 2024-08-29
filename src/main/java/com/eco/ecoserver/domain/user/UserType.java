package com.eco.ecoserver.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserType {
    DEAF("DEAF"), NONDEAF("NONDEAF");

    final String key;
}
