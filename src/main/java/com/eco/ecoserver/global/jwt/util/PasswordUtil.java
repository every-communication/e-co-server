package com.eco.ecoserver.global.jwt.util;

import java.security.SecureRandom;

public class PasswordUtil {

    private static final int PASSWORD_LENGTH = 8; // 비밀번호 길이
    private static final char[] CHAR_SET = new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    }; // 비밀번호 생성에 사용되는 문자 집합

    private static final SecureRandom RANDOM = new SecureRandom(); // 암호화 보안용 SecureRandom

    /**
     * 정해진 길이의 랜덤 비밀번호를 생성합니다.
     *
     * @return 랜덤으로 생성된 비밀번호
     */
    public static String generateRandomPassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH); // 효율적인 문자열 조작을 위해 StringBuilder 사용

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = RANDOM.nextInt(CHAR_SET.length); // CHAR_SET에서 랜덤하게 문자를 선택
            password.append(CHAR_SET[index]); // 선택한 문자를 비밀번호에 추가
        }

        return password.toString(); // StringBuilder를 String으로 변환하여 반환
    }


}
