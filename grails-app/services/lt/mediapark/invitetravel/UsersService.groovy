package lt.mediapark.invitetravel

import grails.transaction.Transactional

@Transactional
class UsersService {

    def loggedInUsers = [:]

    def loginVK(def jsonMap) {
        //get VK stuff
        finishLogin(user)
    }

    def loginFB(def jsonMap) {
        // get FB stuff
        finishLogin(user)
    }

    private void finishLogin(def user) {
        user.lastActive = new Date()
        loggedInUsers << ["${user.id}" : user]
    }

    def updateUser(def userId, def jsonMap) {
        def user = User.get(userId)

        jsonMap.keys.each { it ->
            if (!it.eqauls('id') && !it.equals('name')) {
                if (user.$it) {
                    user.$it = jsonMap.$it
                }
            }
        }

        user.save()
    }

    def logout(def userId) {

    }

    def getUsersList(def userId, def amount, def jsonMap) {
        if (jsonMap?.fresh) {
            loggedInUsers[userId]?.listedIds?.clear()
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
            eq(isValid, true)
            notIn('id') { loggedInUsers[userId]?.listedIds }
        }
    }

    def userReady(def userId) {
        def user = loggedInUsers[userId]
        user?.lastActive = new Date()
        User.exists(userId) && user
    }

    def getUser(def userId) {
        loggedInUsers[userId]?: User.findWhere(isValid: true, id: userId)
    }
}
