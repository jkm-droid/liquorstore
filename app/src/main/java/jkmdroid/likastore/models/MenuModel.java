package jkmdroid.likastore.models;

/**
 * Created by jkmdroid on 6/13/21.
 */
public class MenuModel {
    String itemTitle;
    int iconImage;

    public void setItemTitle(String iconName) {
        this.itemTitle = iconName;
    }

    public void setIconImage(int iconImage) {
        this.iconImage = iconImage;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public int getIconImage() {
        return iconImage;
    }
}
