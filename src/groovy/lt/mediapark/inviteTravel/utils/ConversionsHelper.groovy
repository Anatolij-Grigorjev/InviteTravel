package lt.mediapark.invitetravel.utils

import lt.mediapark.invitetravel.User
import lt.mediapark.invitetravel.Place

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

    public static Map placeToJSONMap(Place place) {
        def placeMap = null
        if (place) {
            placeMap = [:]
            placeMap['id'] = place.placeId
            placeMap['description'] = place.description
        }
        placeMap
    }

}
