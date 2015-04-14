package lt.mediapark.invitetravel

class Picture {

    static constraints = {
        data nullable: false, maxSize: 3840 * 2048
    }

    String mimeType
    String name
    byte[] data
}
