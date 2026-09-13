package com.toxic.search;

final class FavoriteRefreshCoordinator {
    static final SingleFlight checks = new SingleFlight();
    static volatile boolean foreground;
    private FavoriteRefreshCoordinator() {}
}
