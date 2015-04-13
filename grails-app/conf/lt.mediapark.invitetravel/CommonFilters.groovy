package lt.mediapark.invitetravel

import grails.util.Environment
import lt.mediapark.invitetravel.Picture
import lt.mediapark.invitetravel.User

class CommonFilters {

    def usersService

    def filters = {

//        validatePicture(controller: 'pictures', action: '(index|delete)') {
//            before = {
//                def id = params.id
//                return !!Picture.exists(id)
//            }
//        }
//        allowDebugging(controller: 'debug', action: '*') {
//            before = {
//                return !!Environment.isDevelopmentMode()
//            }
//        }
//        controlRequestor(controller: '(users|pictures)', action: '(index|list|logout|update|delete|upload)') {
//            before =  {
//                def userId = params.requestor
//                boolean userThere = usersService.userReady(userId)
//                userThere || redirect(uri: '500')
//            }
//        }
    }
}
