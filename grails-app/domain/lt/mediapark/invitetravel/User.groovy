package lt.mediapark.invitetravel

class User {

    public static final int MAX_PICTURES = 4;

    static constraints = {
        name nullable: false
        level nullable: false
        deviceToken nullable: false, unique: true
        userIdFb unique: true
        userIdVk unique: true
        isValid nullable: false
    }

    static transients = ['listedIds']


    def name
    def description
    Place residence
    def deviceToken
    def userIdFb
    def userIdVk
    List<Place> wantToVisit
    UserLevel level = UserLevel.CANT_PAY
    List<Picture> pictures = []
    List<ChatMessage> messagesForMe = []
    Date lastPayment
    Date lastActive
    def isValid = true
    def defaultPictureId
    List listedIds
}
