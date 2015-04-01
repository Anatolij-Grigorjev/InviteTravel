package lt.mediapark.invitetravel

import grails.transaction.Transactional

@Transactional
class UsersService {

    def loggedInUsers = [:]

    def loginVK(def jsonMap) {

    }

    def loginFB(def jsonMap) {

    }

    def getUsersList() {
        User.findAllWhere([isValid: true], ['sort' : 'lastActive', 'order' : 'desc'])
    }

    def userReady(def userId) {
        User.exists(userId) && loggedInUsers[userId]
    }

    def getUser(def userId) {
        loggedInUsers[userId]?: User.findWhere(isValid: true, id: userId)
    }
}
