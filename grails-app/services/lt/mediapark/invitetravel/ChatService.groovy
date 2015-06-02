package lt.mediapark.invitetravel

import com.relayrides.pushy.apns.util.ApnsPayloadBuilder
import grails.transaction.Transactional
import lt.mediapark.invitetravel.constants.ErrorType
import lt.mediapark.invitetravel.constants.UserLevel
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation

@Transactional
class ChatService {

    def usersService

    private static final int MAX_MESSAGE_CHARS = 45

    def getCorrespondence(def userId1, def userId2, Long requestorNum, Date latest) {

        Long userId1Num = Long.parseLong(userId1)
        Long userId2Num = Long.parseLong(userId2)

        def messages = ChatMessage.createCriteria().list {
            or {
                and {
                    eq('from.id', userId2Num)
                    eq('to.id', userId1Num)
                }
                and {
                    eq('from.id', userId1Num)
                    eq('to.id', userId2Num)
                }
            }
            le('created', latest)
            order('sent', 'asc')
        }
        //exclude messages that don't allow levels to talk
        // (assuming the levels never talked before)
        messages = messages.findAll { msg -> requestorNum == msg.from.id || !!msg.sent }
        .each { it ->
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

    def getChatsList(Long userIdNum) {
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


    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.NESTED)
    def sendMessage(User fromUser, def userToId, String text) {
        def toUser = usersService.get(Long.parseLong(userToId), true)
        ChatMessage message = new ChatMessage(from: fromUser, to: toUser, text: text, created: new Date())
        //validate that message can be sent and error otherwise
        boolean usersHaveBeenTalking = (toUser.hasMessagesFrom(fromUser.id) || fromUser.hasMessagesFrom(toUser.id))
        boolean levelsGood = fromUser.level.canTalkTo(toUser.level)
        boolean subPayGood = fromUser.activeSubMatchLevel()
        if (!usersHaveBeenTalking) {
            if (!levelsGood) {
                message.error = ErrorMessage.findOrCreateWhere(type: ErrorType.BAD_LEVEL, solutions: UserLevel.talkMatrix[(toUser.level)].rank.toListString())
            } else if (!subPayGood) {
                message.error = ErrorMessage.findOrCreateWhere(type: ErrorType.BAD_PAY)
            }
        }
        message = message.save()
        message = message.refresh()
        fromUser = fromUser.refresh()
        toUser = toUser.refresh()
        //only send message to receiving person if they have the level or if they chatted before
        if (usersHaveBeenTalking || levelsGood && subPayGood) {
            message.sent = new Date()
            if (message.error) {
                message.error.delete()
                message.error = null
            }
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


    def refreshUserMessages(User user) {
        ChatMessage.withSession { session ->
            boolean subPayGood = user.activeSubMatchLevel()
            user.messagesFromMe.findAll { !it.sent }
            .each { msg ->
                boolean usersHaveBeenTalking = (msg.to.hasMessagesFrom(user.id) || user.hasMessagesFrom(msg.to.id))
                boolean levelsGood = user.level.canTalkTo(msg.to.level)

                if (usersHaveBeenTalking || levelsGood && subPayGood) {
                    msg.sent = new Date()
                    msg.error = null
                } else {
                    def error = null
                    if (!levelsGood) {
                        error = ErrorMessage.findOrCreateWhere(type: ErrorType.BAD_LEVEL, solutions: UserLevel.talkMatrix[(msg.to.level)].rank.toListString())
                    } else if (!subPayGood) {
                        error = ErrorMessage.findOrCreateWhere(type: ErrorType.BAD_PAY)
                    }
                    if (msg.error && !msg.error.equals(error)) {
                        msg.error = error
                    }
                }
                msg.save()
            }
            session.flush()
//            user.payments.values().findAll { it.valid }
//                    .each { payment ->
//                user.messagesFromMe.findAll { !it.sent }
//                        .each { msg ->
//                    if (payment.subscriptionType.subLevel.canTalkTo(msg.to.level)) {
//                        msg.sent = new Date()
//                        if (msg.error) {
//                            msg.error.delete()
//                        }
//                        msg.save()
//                    }
//                }
//            }
//            //save them messages
//            session.flush()
        }
        user.refresh()
    }
}
