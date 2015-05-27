package lt.mediapark.invitetravel

import lt.mediapark.invitetravel.constants.SubscriptionType
import lt.mediapark.invitetravel.constants.UserLevel

class User {

    public static final int MAX_ACTIVE_PICTURES = 4;

    static mapping = {
        payments lazy: false
        residence lazy: false
        wantToVisit lazy: false
    }

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
            messagesFromMe: ChatMessage,
            payments: Payment
    ]

    static mappedBy = [
            messagesToMe: "to",
            messagesFromMe: "from"
    ]

    //doesn't do jack, shit or cunt as far as hibernate gives a fuck
    static fetchMode = [
            payments: 'eager',
            wantToVisit: 'eager',
            residence: 'eager'
    ]

    String name
    String description = ""
    Place residence
    String deviceToken
    Long userIdFb
    Long userIdVk
    List<Place> wantToVisit = []
    UserLevel level
    List<Picture> pictures = []
    Set<ChatMessage> messagesToMe = []
    Set<ChatMessage> messagesFromMe = []
    Map<String, Payment> payments = [:]
    Date lastActive
    Boolean valid = true


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof User)) return false

        User user = (User) o

        if (deviceToken != user?.deviceToken) return false
        if (id !=          user?.id) return false
        if (level !=       user?.level) return false
        if (name !=        user?.name) return false
        if (userIdFb !=    user?.userIdFb) return false
        if (userIdVk !=    user?.userIdVk) return false
        if (valid !=       user?.valid) return false
        if (version !=     user?.version) return false

        return true
    }

    boolean hasMessagesFrom(Long otherId) {
        return this.messagesToMe?.any { it.from?.id == otherId && it.sent }
    }

    boolean activeSubMatchLevel() {
        if (level == UserLevel.CANT_PAY)
            return true
        payments.findAll { SubscriptionType.getById(it.key).subLevel.equals(level) }.any{ it.value.valid }
    }

    int hashCode() {
        int result
        result = name.hashCode()
        result = 31 * result + (deviceToken? deviceToken.hashCode() : 0)
        result = 31 * result + (userIdFb? userIdFb.hashCode() : 0)
        result = 31 * result + (userIdVk? userIdVk.hashCode() : 0)
        result = 31 * result + (level? level.hashCode() : 0)
        result = 31 * result + (valid? valid.hashCode() : 0)
        result = 31 * result + (id? id.hashCode() : 0)
        result = 31 * result + (version? version.hashCode() : 0)
        return result
    }

    int firstFreeSpace() {
        if (!pictures) {
            return 0
        }
        def indices = pictures.index
        def free = 0
        while (indices.contains(free)) {free++}
        free
    }

    public Long getDefaultPictureId() {
        pictures.find {it.index == 0}?.id
    }
}
