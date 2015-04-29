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
            response.contentType = 'application/octet-stream'
            response.addHeader('Content-disposition', "attachment;filename=${picture.id}")

            response.outputStream << new ByteArrayInputStream(picture.data)
        } else {
            render(status: 404)
        }
    }

    def delete = {

        def pictureId = params.id
        def picture = Picture.get(pictureId)

        if (picture) {
            picture.delete(flush: true)
            render (status: 200)
        } else {
            render (status: 404)
        }
    }

    def upload = {

        CommonsMultipartFile picture = request.getFile('picture')

        def user = User.get(params.requestor)
        def replace = params.replace

        //either get or create a picture
        Picture pic = replace? Picture.get(Long.parseLong(replace)) : new Picture()
        if (!pic) pic = new Picture()
        pic.data = picture.bytes
        pic.mimeType = picture.contentType
        pic.name = picture.name
        if (pic) {
            pic = pic.save(true)
            if (!user.pictures) {
                user.defaultPictureId = pic.id
            }
            user.pictures << pic
            user.save()
            def message = ['pictureId': "${pic?.id}"]
            render message as JSON
        } else {
            def message = ['message' : "Picture ${picture} not saved!"]
            render message as JSON
        }
    }
}
