package lt.mediapark.invitetravel

import lt.mediapark.invitetravel.enums.UserLevel

class User {

    public static final int MAX_ACTIVE_PICTURES = 4;

    static constraints = {
        name nullable: false
        level nullable: false
        deviceToken unique: true
        userIdFb unique: true
        userIdVk unique: true
        valid nullable: false
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
    UserLevel level
    List<Picture> pictures = []
    List<ChatMessage> messagesToMe = []
    List<ChatMessage> messagesFromMe = []
    Date lastPayment
    Date lastActive
    Boolean valid = true
    Long defaultPictureId
    List listedIds = []
}
