package lt.mediapark.invitetravel

class User {

    public static final int MAX_PICTURES = 4;

    static constraints = {
        name nullable: false
        level nullable: false
        deviceToken unique: true
        userIdFb unique: true
        userIdVk unique: true
        userValid nullable: false
    }

    static hasMany = [
            wantToVisit : Place,
            pictures : Picture,
            messagesToMe: ChatMessage,
            messagesFromMe: ChatMessage
    ]

    static mappedBy = [
            messagesToMe: "to",
            messagesFromMe: "from"
    ]

    static transients = ['listedIds']

    String name
    String description
    Place residence
    String deviceToken
    Long userIdFb
    Long userIdVk
    List<Place> wantToVisit
    UserLevel level = UserLevel.CANT_PAY
    List<Picture> pictures = []
    List<ChatMessage> messagesToMe = []
    List<ChatMessage> messagesFromMe = []
    Date lastPayment
    Date lastActive
    Boolean userValid = true
    Long defaultPictureId
    List listedIds = []
}
