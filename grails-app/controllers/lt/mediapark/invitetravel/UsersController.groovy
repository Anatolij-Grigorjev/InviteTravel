package lt.mediapark.invitetravel

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
            } as JSON
        } else {
            render {
                ['message' : "Update failed for user ${params.id}!"]
            } as JSON
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
        render userAttrs as JSON
    }

    def list = {
        render {
            def users = usersService.usersList
        } as JSON
    }

}
