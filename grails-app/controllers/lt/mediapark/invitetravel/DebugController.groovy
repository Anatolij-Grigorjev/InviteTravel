package lt.mediapark.invitetravel

import grails.converters.JSON
import lt.mediapark.invitetravel.enums.Source
import lt.mediapark.invitetravel.enums.UserLevel
import lt.mediapark.invitetravel.utils.ConversionsHelper

class DebugController {

    static allowedMethods = [
            login: 'POST',
            list: 'POST'
    ]

    def cities = ['Paris', 'Belgium', 'Texas', 'Brazil', 'Tokyo', 'Vilnius', 'Stockholm']

    def loginService
    def placesService

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

    def list = {

        def rnd = new Random()

        int amount = rnd.nextInt(25)

        List<Map> dummyList = []
        def users = User.all
        def levels = UserLevel.values()
        def picIds = Picture.all.id
        if (users.size() < amount) {
            (amount - users.size() + 1).times {
                def user = new User()
                def fieldChoice = (rnd.nextBoolean()? 'userIdVk' : 'userIdFb' )
                user.description = "This is a test user created using the Debug controller at ${new Date()}"
                user.level = levels.toList()[rnd.nextInt(levels.size())]
                user.name = "Test Testinsky #${new Date().time}"
                user."$fieldChoice" = Math.abs(rnd.nextLong())
                user.lastActive = new Date()
                if (!user.pictures) user.pictures.addAll(picIds.size() >= User.MAX_ACTIVE_PICTURES? picIds.subList(0, rnd.nextInt(User.MAX_ACTIVE_PICTURES) + 1) : picIds)
                user.residence = placesService.getPlace(cities[rnd.nextInt(cities.size())])
                user = user.save()
                users << user
            }
        }
        users.each {
            loginService.loggedInUsers << ["${it.id}": it]
            dummyList << ConversionsHelper.userToListMap(it)
        }
        dummyList.sort(true) {it.lastActive}

        render dummyList.reverse() as JSON
    }
}
