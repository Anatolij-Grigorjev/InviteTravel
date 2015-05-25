package lt.mediapark.invitetravel.constants

/**
 * Created by anatolij on 31/03/15.
 */
public enum LoginType {

    VK('VK'), FB('FB')

    private LoginType(String jsonString) {
        this.loginString = jsonString
    }

    String loginString
}