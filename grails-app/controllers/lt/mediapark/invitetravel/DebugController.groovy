package lt.mediapark.invitetravel

import grails.converters.JSON
import lt.mediapark.invitetravel.enums.Source
import lt.mediapark.invitetravel.enums.UserLevel

class DebugController {

    static allowedMethods = [
            login: 'POST',
            list: 'POST'
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

    def list = {

        def rnd = new Random()

        int amount = rnd.nextInt(25)

        List<Map> dummyList = []
        def userIds = User.all.id
        def levels = UserLevel.values()
        if (userIds.size() < amount) {
            (amount - userIds.size() + 1).times {
                def user = new User()
                def fieldChoice = (rnd.nextBoolean()? 'userIdVk' : 'userIdFb' )
                user.description = "This is a test user created using the Debug controller at ${new Date()}"
                user.level = levels.toList()[rnd.nextInt(levels.size())]
                user.name = "Test Testinsky #${new Date().time}"
                user."$fieldChoice" = rnd.nextLong()
                user.lastActive = new Date()

                user = user.save()
                userIds << user.id
            }
        }
        def picIds = Picture.all.id
        amount.times {
            Map map = [:]
            map.'id' = userIds[rnd.nextInt(userIds.size())]
            map.'hasMessages' = rnd.nextBoolean()
            map.'level' = levels.rank[rnd.nextInt(levels.size())]
            map.'thumbnail' = picIds? picIds[rnd.nextInt(picIds.size())] : null;
            map.'lastActive' = Math.abs(new Date().time - rnd.nextLong())

            dummyList << map
        }
        dummyList.sort(true) {it.lastActive}

        render dummyList.reverse() as JSON
    }
}
