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
            def message = ['message': 'Picture is gone!']
            render ([status: 404], message) as JSON
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
                def message = ['pictureId': "${pic?.id}"]
                render message as JSON
            } else {
                def message = ['message' : "Picture ${picture} not saved!"]
                render message as JSON
            }
        } else {
            def message = ['message':"No known user has id ${params.id}!"]
            render message as JSON
        }
    }
}
