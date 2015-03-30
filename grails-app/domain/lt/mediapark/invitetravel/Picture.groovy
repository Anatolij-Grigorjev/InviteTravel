package lt.mediapark.invitetravel

class Picture {

    static constraints = {
        path nullable: false, unique: true
    }

    def path
    def mimeType
    byte[] data
}
