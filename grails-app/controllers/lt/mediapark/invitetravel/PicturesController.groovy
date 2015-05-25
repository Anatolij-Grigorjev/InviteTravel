package lt.mediapark.invitetravel

import grails.converters.JSON
import org.springframework.web.multipart.commons.CommonsMultipartFile

class PicturesController {

    static allowedMethods = [
        upload: 'POST',
        delete: 'DELETE',
        index: 'GET'
    ]


    def usersService


    def index = {
        def picture = Picture.get(Long.parseLong(params.id))
        if (picture) {
            response.contentType = 'image/jpeg'
            response.setHeader('Content-disposition', "attachment;filename=${picture.id}.jpg")
            response.contentLength = picture.data.length

            response.outputStream << new ByteArrayInputStream(picture.data)
            response.outputStream.flush()

            return null
        } else {
            render(status: 404)
        }
    }

    def delete = {

        def pictureId = Long.parseLong(params.id)
        def picture = Picture.get(pictureId)
        def user = usersService.get(Long.parseLong(params.requestor))
        if (picture) {
            if (picture.index == 0) {
                if (user.pictures.size() == 1) {
                    //this is default image and no other images available - cant delete, need avatar
                    render (status: 403)
                } else {
                    //this default image and other images are available - make closes image default
                    Picture newAlt = null
                    int goodInd = 1
                    while (!newAlt) {
                        newAlt = user.pictures.find {it.index == goodInd}
                        if (!newAlt) goodInd++
                    }

                    newAlt.index = 0
                    newAlt.save()
                    user.refresh()
                 }
            }
            //this not default image or default image handled - nobody cares
            user.pictures.remove(picture)
            user.save()
            picture.delete(flush: true)
            render (status: 200)
        } else {
            render (status: 404)
        }
    }

    def upload = {

        CommonsMultipartFile picture = request.getFile('picture')

        def user = usersService.get(Long.parseLong(params.requestor))
        int index = Integer.parseInt(params.index)?:0

        //either get or create a picture
        Picture pic = user.pictures.find {it.index == index}?: new Picture(index: index)
        pic.data = picture.bytes
        pic.mimeType = picture.contentType
        pic.name = picture.name
        if (pic) {
            pic = pic.save(true)
            if (!user.pictures) {
                user.defaultPictureId = pic.id
                pic.index = 0
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
