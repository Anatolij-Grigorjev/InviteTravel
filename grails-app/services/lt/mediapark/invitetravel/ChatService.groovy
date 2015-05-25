package lt.mediapark.invitetravel

import com.relayrides.pushy.apns.util.ApnsPayloadBuilder
import grails.transaction.Transactional

@Transactional
class ChatService {

    def usersService

    private static final int MAX_MESSAGE_CHARS = 30

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
            le('created', latest)
            order('sent', 'asc')
        }
        def requestorNum = Long.parseLong(requestorId)
        //exclude messages that don't allow levels to talk
        // (assuming the levels never talked before)
        messages.each { it ->
            if (requestorNum == it.to?.id) {
                def altered = false
                if (!it.to?.messagesToMe.contains(it)) {
                    it.to.messagesToMe << it
                    it.to.save()
                }
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

    def getChatsList(def userId) {
        def userIdNum = Long.parseLong(userId)
        List<ChatMessage> messages = ChatMessage.createCriteria().list {
            or {
                eq('from.id', userIdNum)
                eq('to.id', userIdNum)
            }
            order('sent', 'asc')
        }

        def recentMap = messages.collectEntries {
            def keyToCheck = (it?.from?.id == userIdNum? it?.to?.id : it?.from?.id)
            [(keyToCheck) : it]
        }
        recentMap.values().collect()
    }


    def sendMessage(def userFromId, def userToId, String text) {
        def fromUser = usersService.get(Long.parseLong(userFromId), true)
        def toUser = usersService.get(Long.parseLong(userToId), true)

        ChatMessage message = new ChatMessage(from: fromUser, to: toUser, text: text, created: new Date())
        message = message.save()
        message = message.refresh()
        fromUser = fromUser.refresh()
        toUser = toUser.refresh()
        //only send message to receiving person if they have the level or if they chatted before
        if (toUser.hasMessagesFrom(fromUser.id) || (fromUser.level.canTalkTo(toUser.level) && fromUser.activeSubMatchLevel())) {
            message.sent = new Date()
            message = message.save()
            if (toUser.deviceToken) {
                sendNotification(toUser.deviceToken) { ApnsPayloadBuilder builder ->
                    builder.with {
                        alertTitle = "Message from ${fromUser.name.split('/w+/').first()}:"
                        alertBody = "${text.subSequence(0, MAX_MESSAGE_CHARS)}${text.size() > MAX_MESSAGE_CHARS? '...' : ''}"
                    }
                }
            }
        }
        message
    }
}
