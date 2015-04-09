package lt.mediapark.invitetravel

/**
 * Created by anatolij on 07/04/15.
 */
public enum Source {

    VK("VK", "VKontakte"), FB("FB", "Facebook")

    private final String shortDescr
    private final String longDescr

    private Source(String shortDesc, String longDesc) {
        this.shortDescr = shortDesc
        this.longDescr = longDesc
    }

    public String shortDescription() {
        return this.shortDescr
    }

    public String longDescription() {
        return this.longDescr
    }
}