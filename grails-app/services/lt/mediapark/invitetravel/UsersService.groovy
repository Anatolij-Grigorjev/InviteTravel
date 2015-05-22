package lt.mediapark.invitetravel

import grails.transaction.Transactional
import grails.util.Holders
import lt.mediapark.invitetravel.enums.UserLevel
import org.hibernate.criterion.Order

@Transactional
class UsersService {

    def loginService

    def updateUser(Long userId, Map jsonMap) {
        //nice if user was already cached (also most probable, as you can only update yourself)
        def user = get(userId)
        user = user.lock()
        if (jsonMap.description) user.description = jsonMap.description
        if (jsonMap.level) user.level = UserLevel.findForLevel(jsonMap.level)
        if (jsonMap.residence) user.residence = Place.findOrSaveWhere([placeId: jsonMap.residence.id, description: jsonMap.residence.description])
        if (jsonMap.wantToVisit) {
            user.wantToVisit.clear()
            jsonMap.wantToVisit.each {
                user.wantToVisit << Place.findOrSaveWhere([placeId: it.id, description: it.description])
            }
        }
        //save before moving on to pictures
        user = user.save()
        if (jsonMap.pictures) {
            user.pictures.clear();
            user.pictures.addAll(jsonMap.pictures.collect {Picture.get(it.value)});
        }

        user = user.save()
    }

    def getUsersList(User user, Integer amount, def jsonMap) {
        if (jsonMap?.fresh) {
            user.listedIds?.clear()
        }
        Closure placesCriteria = {
            if (jsonMap?.query) {
                ilike('description', "%${jsonMap.query}%")
            }
            if (jsonMap?.place) {
                like('placeId', "${jsonMap.place?.id}")
                like('description', "%${jsonMap.place?.description}%")
            }
        }
        String searchArea = jsonMap?.searchType == 0? "residence" : "wantToVisit";
        if (!user.listedIds) {user.listedIds << user.id}
        def theList = User.createCriteria().list {
            "${searchArea}"(placesCriteria)
            order('lastActive', 'desc')
            order('pictures', 'desc')
//            order('defaultPictureId', 'desc')
            setMaxResults(amount)
            eq('valid', true)
            not {
                'in'('id', user?.listedIds)
            }
            distinct('id')
        }
        theList
    }

    def boolean userReady(def userId) {
        def user = get(userId)
        user?.lastActive = new Date()
        user && User.exists(userId)
    }

    def User get(Long userId, boolean canBeOffline = false) {
        log.info("User id ${userId} was requested,${canBeOffline? " " : " NOT "}OK to search storage...")
        def user = loginService.loggedInUsers[(userId)]
        if (user) {
            log.info("Fetched ${userId} from cache!")
            user = user.refresh()
        }
        if (!user && canBeOffline) {
            log.info("Fetching ${userId} from storage!")
            user = User.get(userId)
        }
        user
    }
}
