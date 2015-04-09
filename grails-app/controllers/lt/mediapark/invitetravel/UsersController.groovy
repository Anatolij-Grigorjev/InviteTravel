package lt.mediapark.invitetravel

import com.google.gson.Gson
import org.springframework.web.multipart.commons.CommonsMultipartFile

import javax.xml.ws.Response

class UsersController {

    static allowedMethods = [
        login: 'POST',
        list: 'GET',
        update: 'POST',
        logout: 'GET',
        index: 'GET'
    ]

    def usersService

    def index = {
        def user = usersService.getUser(params.id)
        def target = user? {
            def userMap = [:]
            userMap['id'         ] = user?.id
            userMap['name'       ] = user?.name
            userMap['description'] = user?.description
            userMap['residence'  ] = user?.residence
            userMap['level'      ] = user?.level.rank
            userMap['wantToVisit'] = user?.wantToVisit
            userMap['pictures'  ] = [user?.defaultPictureId] << user?.pictures?.collect { it.id } as Set
        } : ['status': 404]
        render target as Gson
    }

    def update = {
        try {
            usersService.updateUser(params.requestor, request.JSON)
            render(status: 200)
        } catch (Exception e) {
            render {
                ['message' : "Update failed for user ${params.requestor}! Reason: ${e.message}"]
            } as Gson
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
                rightLogin = usersService.&loginVK
                break
            case LoginType.FB:
                rightLogin = usersService.&loginFB
                break
        }
        userAttrs[level] = UserLevel.findForLevel(request.JSON.level) ?: UserLevel.CANT_PAY
        userAttrs << request.JSON
        def userInfo = rightLogin(userAttrs)
        render(status: userInfo.fresh? 201 : 200) {
            ['userId' : userInfo.id]
        } as Gson
    }

    def list = {
        render {
            // in this context the id parameter is used to indicate return list size
            //this is more convenient than defining a whole new url mapping, since old
            //one fits just fine
            usersService.getUsersList(params.requestor, params.id, request.JSON).collect { user ->
                [
                 'id'       : user.id,
                 'hasMessages': !!user.unreadMessages,
                 'level'    : user.level,
                 'thumbnail': user.defaultPictureId,
                 'lastActive' : user.lastActive.time
                ]
            }
        } as Gson
    }

}
