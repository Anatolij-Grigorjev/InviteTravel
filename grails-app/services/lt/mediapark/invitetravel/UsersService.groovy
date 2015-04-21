package lt.mediapark.invitetravel

import grails.transaction.Transactional

@Transactional
class UsersService {

    def loginService

    def updateUser(def userId, Map jsonMap) {
        //nice if user was already cached (also most probable, as you can only update yourself)
        def user = loginService.loggedInUsers[userId]?:User.get(userId)

        if (jsonMap.description)
            user.description = jsonMap.description
        if (jsonMap.lastPayment)
            user.lastPayment = new Date(json.lastPayment)
        if (jsonMap.level)
            user.level = jsonMap.level
        if (jsonMap.residence)
            user.residence = new Place(id: jsonMap.residence.id, description: jsonMap.residence.description)
        if (jsonMap.wantToVisit) {
            user.wantToVisit.clear()
            jsonMap.wantToVisit.each {
                user.wantToVisit << new Place(id: it.id, description: it.description)
            }
        }
        if (jsonMap.pictures) {
            user.pictures.clear();
            user.pictures.addAll(jsonMap.pictures)
        }
        user.defaultPictureId = user.pictures[0]?.id?: null;

        user.save()
    }

    def getUsersList(def userId, def amount, def jsonMap) {
        if (jsonMap?.fresh) {
            loginService.loggedInUsers[userId]?.listedIds?.clear()
        }
        Closure placesCriteria = {
            if (jsonMap?.query) {
                like('description', "%${jsonMap.query}%")
            }
            if (jsonMap?.place) {
                like('placeId', "${jsonMap.place?.placeId}")
                like('description', "%${jsonMap.place?.description}%")
            }
        }
        String searchArea = jsonMap?.searchType == 0? "residence" : "wantToVisit";
        Integer amountNum = Integer.parseInt(amount)
        User.createCriteria().list {
            ${searchArea}(placesCriteria)
            order('lastActive', 'desc')
            setMaxResults(amountNum)
            eq('valid', true)
            notIn('id') { loginService.loggedInUsers[userId]?.listedIds }
        }
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
