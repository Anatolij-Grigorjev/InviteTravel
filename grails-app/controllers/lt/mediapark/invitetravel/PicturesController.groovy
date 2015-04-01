package lt.mediapark.invitetravel

import grails.converters.JSON
import org.springframework.web.multipart.commons.CommonsMultipartFile

class PicturesController {

    static allowedMethods = [
        upload: 'POST',
        delete: 'DELETE',
        index: 'GET'
    ]


    def index = {
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

    def delete = {

    }

    def upload = {

        CommonsMultipartFile picture = request.getFile('picture')

        def user = User.get(params.id)

        if (user?.pictures?.size() < User.MAX_PICTURES) {
            Picture pic = new Picture(["data" : picture.bytes, "mimeType" : picture.contentType])
            pic = pic.save()
            if (pic) {
                pic = pic.save(true)
                render {
                    ['pictureId': "${pic?.id}"]
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
}
