package lt.mediapark.invitetravel

class Picture {

    static constraints = {
        data nullable: false
    }

    def mimeType
    byte[] data
}
