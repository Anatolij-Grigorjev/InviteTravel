package lt.mediapark.invitetravel

import grails.transaction.Transactional
import lt.mediapark.invitetravel.enums.UserLevel

@Transactional
class UsersService {

    def loginService

    def updateUser(def userId, Map jsonMap) {
        //nice if user was already cached (also most probable, as you can only update yourself)
        def user = loginService.loggedInUsers[userId]?:User.get(userId)

        if (jsonMap.description) user.description = jsonMap.description
        if (jsonMap.lastPayment) user.lastPayment = new Date(jsonMap.lastPayment)
        if (jsonMap.level) user.level = UserLevel.findForLevel(jsonMap.level)
        if (jsonMap.residence) user.residence = Place.findOrSaveWhere([placeId: jsonMap.residence.id, description: jsonMap.residence.description])
        if (jsonMap.wantToVisit) {
            user.wantToVisit.clear()
            jsonMap.wantToVisit.each {
                user.wantToVisit << Place.findOrSaveWhere([placeId: it.id, description: it.description])
            }
        }
        //save before moving on to pictures
        user.save()
        if (jsonMap.pictures) {
            user.pictures.clear();
            user.pictures.addAll(jsonMap.pictures.collect { Picture.get(it)})
        }
        user.defaultPictureId = user.pictures[0]?.id?: null;

        user.save()
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
                like('placeId', "${jsonMap.place?.placeId}")
                like('description', "%${jsonMap.place?.description}%")
            }
        }
        String searchArea = jsonMap?.searchType == 0? "residence" : "wantToVisit";
        if (!user.listedIds) {user.listedIds << user.id}
        def theList = User.createCriteria().list {
            "${searchArea}"(placesCriteria)
            order('lastActive', 'desc')
            order('defaultPictureId', 'desc')
            setMaxResults(amount)
            eq('valid', true)
            not {
                'in'('id', user?.listedIds)
            }
        }
        theList
    }

    def boolean userReady(def userId) {
        def user = loginService.loggedInUsers[userId]
        user?.lastActive = new Date()
        user && User.exists(userId)
    }

    def User getUser(def userId) {
        loginService.loggedInUsers[userId]?: User.findWhere(valid: true, id: Long.parseLong(userId));
    }
}
