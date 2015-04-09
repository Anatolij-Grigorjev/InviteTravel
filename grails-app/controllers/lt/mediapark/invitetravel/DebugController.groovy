package lt.mediapark.invitetravel

class DebugController {

    static allowedMethods = [
            login: 'POST'
    ]

    def usersService

    def login = {

        def json = request.JSON
        def fieldChoice = (json.source.equals(Source.VK.shortDescription)? 'userIdVk' : 'userIdFb' )
        def user = User.findWhere([fieldChoice : json.userId])

        if (!user) {
            user = new User()
            user.description = "This is a test user created using the Debug controller at ${new Date()}"
            user.level = json.level
            user.name = "Test Testinsky #${new Date().time}"
            user."$fieldChoice" = json.userId

            user.save
        }
        usersService.loggedInUsers[user.id] = user
    }
}
