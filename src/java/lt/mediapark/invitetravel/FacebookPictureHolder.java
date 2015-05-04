package lt.mediapark.invitetravel;

import com.restfb.Facebook;

/**
 * Created by anatolij on 04/05/15.
 */
public class FacebookPictureHolder {

    public FacebookPictureHolder() {
    }

    @Facebook
    FacebookPicture data;

    public FacebookPicture getData() {
        return data;
    }

    public void setData(FacebookPicture data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "FacebookPictureHolder{" +
                "data=" + data +
                '}';
    }
}
