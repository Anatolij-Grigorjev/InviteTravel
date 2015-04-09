package lt.mediapark.invitetravel

class ChatMessage {

    static constraints = {
        all nullable: false
    }

    User from
    User to
    Boolean read = false
    String text
}
