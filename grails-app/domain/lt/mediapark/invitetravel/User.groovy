package lt.mediapark.invitetravel

class User {

    public static final int MAX_PICTURES = 4;

    static constraints = {
        name nullable: false
        level nullable: false
        deviceToken nullable: false, unique: true
        userIdFb unique: true
        userIdVk unique: true
        unreadMessages min: 0, nullable: false
        isValid nullable: false
    }


    def name
    def description
    String placeName
    String placeId
    def deviceToken
    def userIdFb
    def userIdVk
    List wantToVisit
    UserLevel level = UserLevel.CANT_PAY
    List<Picture> pictures = []
    Date lastPayment
    Date lastActive
    def isValid = true
    def defaultPictureId
    int unreadMessages = 0
}
