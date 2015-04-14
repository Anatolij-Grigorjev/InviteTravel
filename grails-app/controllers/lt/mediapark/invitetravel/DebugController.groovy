package lt.mediapark.invitetravel

import grails.converters.JSON
import lt.mediapark.inviteTravel.enums.Source
import lt.mediapark.inviteTravel.enums.UserLevel

class DebugController {

    static allowedMethods = [
            login: 'POST'
    ]

    def loginService

    def login = {

        def json = request.JSON
        def systemId = ((Number)json.userId).longValue()
        def fieldChoice = (json.source.equals(Source.VK.shortDescription())? 'userIdVk' : 'userIdFb' )
        def user = User.findWhere(["${fieldChoice}" : systemId])

        if (!user) {
            user = new User()
            user.description = "This is a test user created using the Debug controller at ${new Date()}"
            user.level = UserLevel.findForLevel(json.level)
            user.name = "Test Testinsky #${new Date().time}"
            user."$fieldChoice" = systemId
            user.lastActive = new Date()

            user = user.save()
        }
        loginService.loggedInUsers[user.id] = user
        def result = ['userId' : user.id]
        render result as JSON
    }
}