package lt.mediapark.invitetravel

import grails.transaction.Transactional

@Transactional
class UsersService {

    def loggedInUsers = [:]

    def loginVK(def jsonMap) {

    }

    def loginFB(def jsonMap) {

    }

    def updateUser(def userId, def jsonMap) {
        def user = User.get(userId)

        user.save()
    }

    def getUsersList(def userId, def amount, def jsonMap) {
        User.createCriteria().list([sort : 'lastActive', order : 'desc', max: amount]) {
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
            eq(isValid, true)
            notIn('id') { loggedInUsers[userId]?.listedIds }
        }
    }

    def userReady(def userId) {
        User.exists(userId) && loggedInUsers[userId]
    }

    def getUser(def userId) {
        loggedInUsers[userId]?: User.findWhere(isValid: true, id: userId)
    }
}
