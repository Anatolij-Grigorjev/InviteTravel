package lt.mediapark.invitetravel

import org.springframework.web.multipart.commons.CommonsMultipartFile
import grails.converters.JSON

class UsersController {

    static allowedMethods = [
            create: 'POST',
            uploadPhoto: 'POST',
            list: 'GET',
            picture: 'GET',
            update: 'POST'

    ]

    def update = {
        def user = User.get(params.id)

        request.JSON.with { key, value ->
            if (user.class.getField(key)) {
                user.${key} = value
            }
        }

        if (user.save(true)) {
            render ['message' : 'OK'] as JSON
        } else {
            render ['message' : "Update failed for user ${params.id}!"]
        }

    }

    def create = {
        def user = new User(params)

        def valid = user.save()
        render {
            if (valid) {
                valid.id
            } else {
                ['message': 'error in saving']
            }
        } as JSON
    }

    def uploadPhoto = {

        CommonsMultipartFile picture = request.getFile('picture')

        def user = User.get(id)

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
        def picture = Picture.get(params.id)
        if (picture) {
            response.contentType 'application/octet-stream'
            response.addHeader('Content-disposition', "attachment;filename=${picture.id}")

            response.outputStream << new ByteArrayInputStream(picture.data)
        } else {
            render {
                ['message': 'Picture is gone!']
            } as JSON
        }
    }

    def list = {
        render {
            User.all.collect { user ->
                ['id' : user.id,
                'hasMessages' : !!user.unreadMessages,
                'level' : user.level,
                'thumbnail' : user.defaultPicturePath
                ]
            }
        } as JSON
    }

}
