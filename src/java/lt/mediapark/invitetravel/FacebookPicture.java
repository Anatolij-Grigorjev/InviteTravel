package lt.mediapark.invitetravel;

import com.restfb.Facebook;

/**
 * Created by anatolij on 04/05/15.
 */
public class FacebookPicture {

    @Facebook
    String url;

    @Facebook
    Integer width;

    @Facebook
    Integer height;

    @Facebook(value = "is_silhouette")
    Boolean isSilhouette;

}
