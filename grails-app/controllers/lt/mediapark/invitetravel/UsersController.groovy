package lt.mediapark.invitetravel

import grails.converters.JSON
import lt.mediapark.invitetravel.constants.LoginType

class UsersController {

    static allowedMethods = [
        login: 'POST',
        list: 'POST',
        update: 'POST',
        logout: 'GET',
        index: 'GET',
        delete: 'DELETE'
    ]

    def usersService
    def loginService
    def JSONConversionService

    def index = {
        def user = usersService.get(Long.parseLong(params.id), true)
        if (user) {
            def target = JSONConversionService.userToMap(user)
            render target as JSON
        } else {
            render (status: 404)
        }
    }

    def update = {
        try {
            def user = usersService.updateUser((User)params.currUser, request.JSON)
            def userMap = JSONConversionService.userToMap(user)
            render userMap as JSON
        } catch (Exception e) {
            log.warn('Failed to update user!', e)
            def message = ['message' : "Update failed for user ${params.requestor}! Reason: ${e.message}"]
            render message as JSON
        }
    }

    def logout = {
        loginService.logout(params.currUser.id)
        render(status: 200)
    }

    def login = {

        def loginType = LoginType.valueOf(request.JSON.source)
        def userAttrs = new LinkedHashMap()
        def rightLogin = null;
        switch (loginType) {
            case LoginType.VK:
                rightLogin = loginService.&loginVK
                break
            case LoginType.FB:
                rightLogin = loginService.&loginFB
                break
        }
        if (request.JSON.level) userAttrs['level'] = request.JSON.level
        userAttrs << request.JSON
        def userInfo = rightLogin(userAttrs)
        //return empty JSON object instead of userId if no level supplied when asking for user
        def result = [:]
        if (userInfo) {
            result << ['userId' : userInfo.userId]
        } else {
            result << ['message': "User with ${request.JSON.source} ID" +
                    " ${request.JSON.userId} not yet known to system and the lack of a provided level did not make things" +
                    " less awkward!"]
        }
        render result as JSON
    }

    def list = {
        User currUser = params.currUser
        def amount = Integer.parseInt(params.id)
        // in this context the id parameter is used to indicate return list size
        //this is more convenient than defining a whole new url mapping, since old
        //one fits just fine
        //the JSON map has list parameters (is it fresh, what was the query, what places are we searching in user)
        currUser.attach()
        def usersList = usersService.getUsersList(currUser, amount, request.JSON).collect { User user ->
            UsersService.userListedIds[currUser.id] << user.id
//            user.attach()
            JSONConversionService.userToListMap(user, currUser)
        }
        def usersMap = ['users' : usersList]
       render usersMap as JSON
    }


    def delete = {
        User currUser = params.currUser
        //disable user validity since nobody minds keeping them around
        currUser.valid = false
        currUser.save()
        loginService.logout(currUser.id)
    }

}
