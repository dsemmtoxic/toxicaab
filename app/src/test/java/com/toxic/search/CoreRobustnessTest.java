package com.toxic.search;

import org.junit.Test;

public final class CoreRobustnessTest {
    @Test public void concurrencyAndCompatibility() throws Exception {
        CoreRobustnessChecks.runAll();
    }
}
