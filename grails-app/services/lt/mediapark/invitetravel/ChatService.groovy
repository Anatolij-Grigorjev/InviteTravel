package lt.mediapark.invitetravel

import com.relayrides.pushy.apns.util.ApnsPayloadBuilder
import grails.transaction.Transactional

@Transactional
class ChatService {

    def usersService

    private static final int MAX_MESSAGE_CHARS = 30;

    def getCorrespondence(def userId1, def userId2, def requestorId, Date latest) {

        def messages = ChatMessage.createCriteria().list {
            or {
                and {
                    eq('from.id', Long.parseLong(userId2))
                    eq('to.id', Long.parseLong(userId1))
                }
                and {
                    eq('from.id', Long.parseLong(userId1))
                    eq('to.id', Long.parseLong(userId2))
                }
            }
            order('sent', 'asc')
        }
        def requestorNum = Long.parseLong(requestorId)
        messages.each { it ->
            if (requestorNum == it.to.id) {
                def altered = false
                if (!it.received) {
                    it.received = new Date()
                    altered = true
                }
                if (!it.read) {
                    it.read = true
                    altered = true
                }
                if (altered) {
                    it.save()
                }
            }
        }
        messages
    }


    def sendMessage(def userFromId, def userToId, String text) {
        def fromUser = usersService.getUser(userFromId)
        def toUser = usersService.getUser(userToId)

        ChatMessage message = new ChatMessage(from: fromUser, to: toUser, text: text, sent: new Date())
        fromUser.messagesFromMe << message
        toUser.messagesToMe << message
        fromUser.save()
        toUser.save()
        if (toUser.deviceToken) {
            sendNotification(toUser.deviceToken) { ApnsPayloadBuilder builder ->
                builder.with {
                    alertTitle = "Message from ${fromUser.name.split('/w+/').first()}:"
                    alertBody = "${text.subSequence(0, MAX_MESSAGE_CHARS)}${text.size() > MAX_MESSAGE_CHARS? '...' : ''}"
                }
            }
        }
    }
}
