package lt.mediapark.invitetravel

import org.springframework.web.multipart.commons.CommonsMultipartFile
import grails.converters.JSON

class UsersController {

    static allowedMethods = [
            create: 'POST',
            uploadPhoto: 'POST',
            list: 'GET',
            picture: 'GET'

    ]

    def create = {
        def user = new User(params)



        def valid = user.save()

        render {
            if (valid) {
                valid.id
            } else {
                'error in saving'
            }
        } as JSON
    }

    def uploadPhoto = {

        CommonsMultipartFile picture = request.getFile('picture')

        def user = User.find {
            it.id.equals(params.id)
        }
        if (user?.pictures?.size() < User.MAX_PICTURES) {
            Picture pic = new Picture(["data" : picture.bytes, "mimeType" : picture.contentType])
            pic = pic.save()
            if (pic) {
                pic.path = "/users/picture/${pic.id}"
                pic = pic.save(true)
                render {
                    ['message': "Picture at path: ${pic?.path}"]
                } as JSON
            } else {
                render {
                    ['message' : "Picture ${picture} not saved!"]
                } as JSON
            }
        } else {
            render {
                ['message':"No known user has id ${params.id}!"]
            } as JSON
        }
    }

    def picture = {

    }

    def list = {
        render {
            User.all.collect { user ->
                ['id' : user.id,
                'hasMessages' : !!user.unreadMessages,
                'level' : user.level,
                ]
            }
        } as JSON
    }

}
