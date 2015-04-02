package lt.mediapark.invitetravel

class Place {

    static constraints = {
        description nullable: false
        placeId nullable: false, unique: true
    }

    String description
    String placeId
}
