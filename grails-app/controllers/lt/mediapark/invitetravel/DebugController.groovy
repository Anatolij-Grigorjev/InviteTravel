package lt.mediapark.invitetravel

import grails.converters.JSON
import lt.mediapark.invitetravel.constants.Source
import lt.mediapark.invitetravel.constants.UserLevel
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

        List<Map> dummyList = []
        def users = User.all
        int newAmount = params.num? Integer.parseInt(params.num) : 25 - users.size() + 1
        def levels = UserLevel.values()
        newAmount.times {
            def user = new User()
            def fieldChoice = (rnd.nextBoolean()? 'userIdVk' : 'userIdFb' )
            user.description = "This is a test user created using the Debug controller at ${new Date()}"
            user.level = levels.toList()[rnd.nextInt(levels.size())]
            user.name = "Test Testinsky #${new Date().time}"
            user."$fieldChoice" = Math.abs(rnd.nextLong())
            user.lastActive = new Date()
            //add random pictures and places to user
            user.residence = placesService.getPlace(cities[rnd.nextInt(cities.size())])
            log.debug "Making user ${user.dump()}"
            if (!user.pictures) {
                (1 + rnd.nextInt(User.MAX_ACTIVE_PICTURES - 1)).times {
                    File image = downloadImage('http://lorempixel.com/320/320/')
                    if (image) {
                        Picture picture = new Picture(data: image.bytes, name: image.name, mimeType: 'image/png', index: user.firstFreeSpace())
                        picture = picture.save()
                        user.pictures << picture
                    }
                }
            }
            user = user.save()
            users << user
        }
        users.each {
            loginService.loggedInUsers << [(it.id): it]
            dummyList << ConversionsHelper.userToListMap(it)
        }
        dummyList.sort(true) {it.lastActive}
        def dummyMap = ['users' : dummyList.reverse()]
        render dummyMap as JSON
    }
}
