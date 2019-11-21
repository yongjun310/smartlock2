package com.smart.lock.common;
/**
 * Created with IntelliJ IDEA.
 * User: xun.wang
 * Date: 15-3-25
 * Time: 上午11:27
 * To change this template use File | Settings | File Templates.
 */

public enum FavoriteTypeEnum {

    NO_FEEL(0), FAVORITE(1), DISLIKE(2), VIEWED(3);
    private int favoriteType;

    FavoriteTypeEnum(int favoriteType) {
        this.favoriteType = favoriteType;
    }

    public int getValue() {
        return this.favoriteType;
    }

}
