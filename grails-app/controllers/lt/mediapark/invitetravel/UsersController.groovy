package lt.mediapark.invitetravel

import com.google.gson.Gson
import org.springframework.web.multipart.commons.CommonsMultipartFile
import grails.converters.JSON

class UsersController {

    static allowedMethods = [
        login: 'POST',
        list: 'GET',
        update: 'POST',
        index: 'GET'
    ]


    def usersService

    def index = {
        render usersService.getUser(params.id).with {
            [
                'id' : id,
                'name' : name,
                'description' : description,
                'residence' : residence,
                'level' : level.rank,
                'wantToVisit' : wantToVisit,
                'pictures' : [defaultPictureId] << pictures?.collect { it.id } as Set
            ]
        } as GSON
    }

    def update = {
        def user = User.get(params.id)

        request.JSON.with { key, value ->
            if (user.class.getField(key)) {
                user."${key}" = value
            }
        }

        if (user.save(true)) {
            render {
                ['message' : 'OK']
            } as GSON
        } else {
            render {
                ['message' : "Update failed for user ${params.id}!"]
            } as GSON
        }

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
        userAttrs << rightLogin(request.JSON)
        render userAttrs as GSON
    }

    def list = {
        render {
            usersService.usersList.collect { user ->
                [
                 'id'       : user.id,
                 'hasMessages': !!user.unreadMessages,
                 'level'    : user.level,
                 'thumbnail': user.defaultPictureId,
                 'lastActive' : user.lastActive.time
                ]
            }
        } as GSON
    }

}
