package lt.mediapark.invitetravel.utils

import groovy.transform.CompileStatic
import lt.mediapark.invitetravel.ChatMessage
import lt.mediapark.invitetravel.Payment
import lt.mediapark.invitetravel.Place
import lt.mediapark.invitetravel.User
import lt.mediapark.invitetravel.constants.SubscriptionType
import org.hibernate.Hibernate

/**
 * Created by anatolij on 27/04/15.
 */
@CompileStatic
class ConversionsHelper {


    public Map userToMap(User user) {
        def map = [:]
        map['id'         ] = user.id
        map['name'       ] = user.name
        map['description'] = user.description?:""
        map['residence'  ] = placeToJSONMap(user.residence)
        map['level'      ] = user.level.rank
        map['wantToVisit'] = user.wantToVisit.collect { placeToJSONMap(it) }
        map['pictures'   ] = user.pictures.collectEntries {[(it.index) : it.id]}
        map['lastActive '] = user.lastActive.time
        map['payments'   ] = paymentsMap(user.payments)
        map
    }

    public Map paymentsMap(Map<String, Payment> payments) {
        payments.collectEntries {["${SubscriptionType.getById(it.key)?.subLevel.rank}" : it.value?.subscriptionEnd.time]}
    }

    public Map userToListMap(User user, User currUser = null) {
        def map = userToMap(user)
        map['hasMessages'] = currUser? currUser.hasMessagesFrom(user.id) && currUser.messagesToMe.any {!it.read} : false
        map
    }

    public Map userToMessagePart(User user) {
        def map = [:]
        map['id'] = user?.id
        map['picId'] = user?.defaultPictureId
        map['name'] = user?.name
        map['level'] = user?.level?.rank
        map.findAll { it.value != null }
    }

    public Map placeToJSONMap(Place place) {
        Map placeMap = null
        if (place) {
            placeMap = [:]
            placeMap['place_id'] = place.placeId
            placeMap['description'] = place.description
        }
        placeMap
    }


    public Map messageToMap(ChatMessage message) {
        def map = [:]
        map['text'] = message?.text
        map['from'] = userToMessagePart(message?.from)
        map['to'] = userToMessagePart(message?.to)
        map['sent'] = message?.sent?.time
        map['created'] = message?.created?.time
        map['read'] = message?.read
        map['received'] = message?.received?.time
        //there is a possible false boolean in there, gotta avoid removing it
        map.findAll { (it.value != null) }
    }

}
