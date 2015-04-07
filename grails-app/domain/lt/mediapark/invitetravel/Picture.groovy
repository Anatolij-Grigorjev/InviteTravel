package lt.mediapark.invitetravel

class Picture {

    static constraints = {
        data nullable: false
    }

    String mimeType
    byte[] data
}
