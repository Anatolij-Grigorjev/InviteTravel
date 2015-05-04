package lt.mediapark.invitetravel

class ChatMessage {

    static constraints = {
        from nullable: false
        to nullable: false
        text nullable: false, maxSize: 8400
        read nullable: false
        sent nullable: false
    }

    User from
    User to
    Boolean read = false
    String text
    Date sent
    Date received
}
