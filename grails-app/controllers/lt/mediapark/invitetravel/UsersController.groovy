package lt.mediapark.invitetravel

import grails.converters.JSON
import lt.mediapark.invitetravel.enums.LoginType

class UsersController {

    static allowedMethods = [
        login: 'POST',
        list: 'POST',
        update: 'POST',
        logout: 'GET',
        index: 'GET'
    ]

    def usersService
    def loginService

    def index = {
        def user = usersService.getUser(params.id)
        if (user) {
            def target =  {
                def userMap = [:]
                userMap['id'         ] = user?.id
                userMap['name'       ] = user?.name
                userMap['description'] = user?.description
                userMap['residence'  ] = user?.residence
                userMap['level'      ] = user?.level.rank
                userMap['wantToVisit'] = user?.wantToVisit
                userMap['pictures'  ] = {
                    Set set = [user?.defaultPictureId]
                    set.addAll(user?.pictures?.collect { it.id })
                    set.findAll { it != null }
                }.call()
                userMap
            }.call()
            render target as JSON
        } else {
            render (status: 404)
        }
    }

    def update = {
        try {
            def user = usersService.updateUser(params.requestor, request.JSON)
            render(status: 200)
        } catch (Exception e) {
            log.warn('Failed to update user!', e)
            def message = ['message' : "Update failed for user ${params.requestor}! Reason: ${e.message}"]
            render message as JSON
        }
    }

    def logout = {
        usersService.logout(params.requestor)
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
        userAttrs[level] = request.JSON.level
        userAttrs << request.JSON
        def userInfo = rightLogin(userAttrs)
        def result = ['userId' : userInfo.id]
        render([status: userInfo.fresh? 201 : 200], result) as JSON
    }

    def list = {
        // in this context the id parameter is used to indicate return list size
        //this is more convenient than defining a whole new url mapping, since old
        //one fits just fine
        def usersList = usersService.getUsersList(params.requestor, params.id, request.JSON).collect { User user ->
            [
             'id'       : user.id,
             'hasMessages': user.messagesToMe.any {!it.read},
             'level'    : user.level,
             'thumbnail': user.defaultPictureId,
             'lastActive' : user.lastActive.time
            ]
        }
       render usersList as JSON
    }

}
