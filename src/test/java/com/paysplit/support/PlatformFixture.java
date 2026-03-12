package com.paysplit.support;

import com.paysplit.db.domain.Platform;

public class PlatformFixture {
    public static Platform activePlatform() {
        return Platform.builder()
                .name("TESTott구독서비스")
                .build();
    }
}
