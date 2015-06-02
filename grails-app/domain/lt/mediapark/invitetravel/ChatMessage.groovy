package lt.mediapark.invitetravel

class ChatMessage {

    static constraints = {
        from nullable: false
        to nullable: false
        text nullable: false, maxSize: 8400
        read nullable: false
        created nullable: false
    }

    static hasOne = [
        'error' : ErrorMessage
    ]

    User from
    User to
    Boolean read = false
    String text
    Date created = new Date()
    Date sent
    Date received
    ErrorMessage error

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof ChatMessage)) return false

        ChatMessage that = (ChatMessage) o

        if (id != that.id) return false
        if (received != that.received) return false
        if (sent != that.sent) return false
        if (text != that.text) return false
        if (version != that.version) return false

        return true
    }

    int hashCode() {
        int result
        result = (text != null ? text.hashCode() : 0)
        result = 31 * result + (sent != null ? sent.hashCode() : 0)
        result = 31 * result + (received != null ? received.hashCode() : 0)
        result = 31 * result + (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        return result
    }
}
