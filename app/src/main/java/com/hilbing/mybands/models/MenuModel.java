package com.hilbing.mybands.models;

public class MenuModel {

    public String menuName;
    public boolean hasChildren;
    public boolean isGroup;

    public MenuModel(String menuName, boolean hasChildren, boolean isGroup) {
        this.menuName = menuName;
        this.hasChildren = hasChildren;
        this.isGroup = isGroup;
    }
}
