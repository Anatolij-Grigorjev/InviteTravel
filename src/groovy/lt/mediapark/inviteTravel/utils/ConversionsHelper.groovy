package lt.mediapark.invitetravel.utils

import lt.mediapark.invitetravel.ChatMessage
import lt.mediapark.invitetravel.Place
import lt.mediapark.invitetravel.User

/**
 * Created by anatolij on 27/04/15.
 */
class ConversionsHelper {

    public static Map userToMap(User user) {
        def map = [:]
        map['id'         ] = user.id
        map['name'       ] = user.name
        map['description'] = user.description?:""
        map['residence'  ] = ConversionsHelper.placeToJSONMap(user.residence)
        map['level'      ] = user.level.rank
        map['wantToVisit'] = user.wantToVisit.collect { ConversionsHelper.placeToJSONMap(user) }
        map['pictures'   ] = user.pictures.id
        map['lastActive '] = user.lastActive.time
        map
    }

    public static Map userToListMap(User user) {
        def map = userToMap(user)
        map['hasMessages'] = user.messagesToMe.any {!it.read}
        map
    }

    public static Map userToMessagePart(User user) {
        def map = [:]
        map['id'] = user?.id
        map['picId'] = user?.defaultPictureId
        map['name'] = user?.name
        map['level'] = user?.level?.rank
        map.findAll { it.value }
    }

    public static Map placeToJSONMap(Place place) {
        def placeMap = null
        if (place) {
            placeMap = [:]
            placeMap['id'] = place.placeId
            placeMap['description'] = place.description
        }
        placeMap
    }


    public static Map messageToMap(ChatMessage message) {
        def map = [:]
        map['text'] = message?.text
        map['from'] = userToMessagePart(message?.from)
        map['to'] = userToMessagePart(message?.to)
        map['sent'] = message?.sent?.time
        map['read'] = message?.read
        map['received'] = message?.received?.time
        map.findAll { it.value }
    }

}
