package lt.mediapark.invitetravel

import grails.transaction.Transactional

@Transactional
class UsersService {

    def loginService

    def updateUser(def userId, def jsonMap) {
        //nice if user was already cached (also most probable)
        def user = loginService.loggedInUsers[userId]?:User.get(userId)

        jsonMap.keys.each { it ->
            if (!it.eqauls('id') && !it.equals('name')) {
                if (user.$it) {
                    user.$it = jsonMap.$it
                }
            }
        }

        user.save()
    }

    def getUsersList(def userId, def amount, def jsonMap) {
        if (jsonMap?.fresh) {
            loginService.loggedInUsers[userId]?.listedIds?.clear()
        }
        User.createCriteria().list {
            if (jsonMap?.searchType == 0) {
                residence {
                    if (jsonMap?.query) {
                        like('description', "%${jsonMap.query}%")
                    }
                    if (jsonMap?.place) {
                        like('placeId', "${jsonMap.place?.placeId}")
                        like('description', "%${jsonMap.place?.description}%")
                    }
                }
            }
            if (jsonMap?.searchType == 1) {
                wantToVisit {
                    if (jsonMap?.query) {
                        like('description', "%${jsonMap.query}%")
                    }
                    if (jsonMap?.place) {
                        like('placeId', "${jsonMap.place?.placeId}")
                        like('description', "%${jsonMap.place?.description}%")
                    }
                }
            }
            order('lastActive', 'desc')
            maxResults(amount)
            eq(valid, true)
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
