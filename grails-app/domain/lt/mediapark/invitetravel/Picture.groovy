package lt.mediapark.invitetravel

class Picture {

    static constraints = {
        path nullable: false, unique: true
        id nullable: false, unique: true
    }

    def id
    def path
    def mimeType
    byte[] data
}
