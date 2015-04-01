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
        User.findAllWhere([isValid: true], ['sort' : 'lastActive', 'order' : 'desc']).collect { user ->
            ['id' : user.id,
             'hasMessages' : !!user.unreadMessages,
             'level' : user.level,
             'thumbnail' : user.defaultPictureId,
             'lastActive' : user.lastActive.time
            ]
        }
    }

    def userReady(def userId) {
        User.exists(userId) && loggedInUsers[userId]
    }
}
