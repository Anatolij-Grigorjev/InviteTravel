package lt.mediapark.invitetravel

class User {

    public static final int MAX_PICTURES = 4;

    static constraints = {
        name nullable: false
        level nullable: false
        deviceToken nullable: false, unique: true
        lastPayment nullable: false
        unreadMessages min: 0, nullable: false
    }

    def name
    def description
    String placeName
    String placeId
    def deviceToken
    List wantToVisit
    UserLevel level
    List<Picture> pictures = []
    Date lastPayment
    Date lastActive
    String defaultPicturePath
    int unreadMessages
}
