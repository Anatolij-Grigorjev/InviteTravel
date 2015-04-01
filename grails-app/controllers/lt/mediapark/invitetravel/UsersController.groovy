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
        userAttrs[level] = UserLevel.findForLevel(request.JSON.level) ?: UserLevel.CANT_PAY
        switch (loginType) {
            case LoginType.VK:

                break
            case LoginType.FB:

                break
        }
    }

    def list = {
        render {
            def users = User.all.collect { user ->
                ['id' : user.id,
                'hasMessages' : !!user.unreadMessages,
                'level' : user.level,
                'thumbnail' : user.defaultPictureId,
                'lastActive' : user.lastActive.time
                ]
            }.sort { -1 * it.lastActive.time }//sort in reverse order

        } as JSON
    }

}
