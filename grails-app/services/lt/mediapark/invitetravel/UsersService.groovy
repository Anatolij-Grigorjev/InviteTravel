package lt.mediapark.invitetravel

import grails.transaction.Transactional
import lt.mediapark.invitetravel.constants.UserLevel
import org.hibernate.NullPrecedence
import org.hibernate.criterion.Order


@Transactional
class UsersService {

    def loginService

    public static Map<Long, Set<Long>> userListedIds = [:]

    def updateUser(User user, Map jsonMap) {
        if (jsonMap.description) user.description = jsonMap.description
        if (jsonMap.level) user.level = UserLevel.findForLevel(jsonMap.level)
        if (jsonMap.residence) user.residence = Place.findOrSaveWhere([placeId: jsonMap.residence.place_id, description: jsonMap.residence.description])
        if (jsonMap.wantToVisit) {
            user.wantToVisit.clear()
            jsonMap.wantToVisit.each {
                user.wantToVisit << Place.findOrSaveWhere([placeId: it.place_id, description: it.description])
            }
        }
        //save before moving on to pictures
        if (jsonMap.pictures) {
            user.pictures.clear();
            user.pictures.addAll(jsonMap.pictures.collect {Picture.get(it.value)});
        }

        user = user.save(flush: true)
        log.info("User these days: ${user.dump()}")
        user
    }

    def getUsersList(User user, Integer amount, def jsonMap) {
        if (jsonMap?.fresh) {
            userListedIds[user.id]?.clear()
        }
        Closure placesCriteria = {
            if (jsonMap?.query) {
                ilike('description', "%${jsonMap.query}%")
            }
            if (jsonMap?.place) {
                like('placeId', "${jsonMap.place?.id}")
                ilike('description', "%${jsonMap.place?.description}%")
            }
        }
        String searchArea = jsonMap?.searchType == 0? "residence" : "wantToVisit";
        if (!userListedIds[user.id]) {
            userListedIds[user.id] = [] as Set
            userListedIds[user.id] << user.id
        }
        def theList = User.createCriteria().list {
            "${searchArea}"(placesCriteria)
            order('lastActive', 'desc')
            order(Order.desc('pictures').nulls(NullPrecedence.LAST))
            setMaxResults(amount)
            eq('valid', true)
            not {
                'in'('id', userListedIds[user.id])
            }
        }
        theList as Set
    }

    def boolean userReady(def userId) {
        def user = get(userId)
        user?.lastActive = new Date()
        user && User.exists(userId)
    }

    def User get(Long userId, boolean canBeOffline = false) {
        log.info("User id ${userId} was requested,${canBeOffline? " " : " NOT "}OK to search logged in users...")
        def user
        if (loginService.loggedInUsers.contains(userId)) {
            log.info("Fetching ${userId} from cache!")
            user = User.get(userId)
        }
        if (!userId && canBeOffline) {
            log.info("Fetching ${userId} from storage!")
            user = User.findById(userId)
        }
        user
    }
}
