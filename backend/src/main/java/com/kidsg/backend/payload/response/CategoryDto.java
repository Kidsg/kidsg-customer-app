package com.kidsg.backend.payload.response;

public class CategoryDto {
    private String id;
    private String name;
    private String slug;
    private String iconName;
    private int displayOrder;
    private boolean isActive;

    public CategoryDto() {
    }

    public CategoryDto(String id, String name, String slug, String iconName, int displayOrder, boolean isActive) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.iconName = iconName;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
