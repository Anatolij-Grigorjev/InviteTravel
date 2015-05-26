package lt.mediapark.invitetravel;

import com.restfb.Facebook;

/**
 * Created by anatolij on 04/05/15.
 */
public class FacebookPicture {

    @Facebook
    private String url;

    @Facebook
    private Integer width;

    @Facebook
    private Integer height;

    @Facebook(value = "is_silhouette")
    private Boolean isSilhouette;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Boolean getIsSilhouette() {
        return isSilhouette;
    }

    public void setIsSilhouette(Boolean isSilhouette) {
        this.isSilhouette = isSilhouette;
    }

    @Override
    public String toString() {
        return "FacebookPicture{" +
                "url='" + url + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", isSilhouette=" + isSilhouette +
                '}';
    }
}
