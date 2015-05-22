package lt.mediapark.invitetravel

class Picture {

    static constraints = {
        data nullable: false, maxSize: 3840 * 2048
        index nullable: false, max: (User.MAX_ACTIVE_PICTURES - 1), min: 0
    }

    String mimeType
    String name
    byte[] data
    int index
}
