package lt.mediapark.invitetravel

import lt.mediapark.invitetravel.Picture
import lt.mediapark.invitetravel.User

class UsersFilters {

    def usersService

    def filters = {
//        allowLogin(controller: 'users', action: 'login', invert: true) {
//            before =  {
//                def userId = params.requestor
//                boolean userThere = usersService.userReady(userId)
//                userThere || redirect(uri: '500')
//            }
//        }
        validatePicture(controller: 'pictures', action: '(index|delete)') {
            before = {
                def id = params.id
                return !!Picture.exists(id)
            }
        }
        allowDebugging(controller: 'debug', action: '*') {
            before = {
                return true;
            }
        }
    }
}
