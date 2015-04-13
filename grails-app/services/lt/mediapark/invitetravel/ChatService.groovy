package lt.mediapark.invitetravel

import com.relayrides.pushy.apns.util.ApnsPayloadBuilder
import grails.transaction.Transactional

@Transactional
class ChatService {

    def usersService

    private static final int MAX_MESSAGE_CHARS = 30;

    def getCorrespondence(def userFromId, def userToId, Date latest) {

        def fromUser = usersService.getUser(userFromId)
        def toUser = usersService.getUser(userToId)

        def messages = ChatMessage.findAllByFromAndToAndSentGreaterThan(fromUser, toUser, latest)
        messages
    }


    def sendMessage(def userFromId, def userToId, String text) {
        def fromUser = usersService.getUser(userFromId)
        def toUser = usersService.getUser(userToId)

        ChatMessage message = new ChatMessage(from: fromUser, to: toUser, text: text, sent: new Date())
        fromUser.messagesFromMe << message
        toUser.messagesToMe << message
        message.save()
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
