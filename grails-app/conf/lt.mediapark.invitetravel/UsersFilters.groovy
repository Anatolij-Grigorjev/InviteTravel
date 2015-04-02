package invitetravel

import lt.mediapark.invitetravel.Picture
import lt.mediapark.invitetravel.User

class UsersFilters {

    def usersService

    def filters = {
        validateWithUser(controller: 'users', action: 'login', invert: true) {
            before = {
                def userId = params.requestor
                return usersService.userReady(userId)
            }
        }
        validatePicture(controller: 'pictures', action: 'index|delete') {
            before {
                def id = params.id
                return !!Picture.exists(id)
            }
        }
        onlyChangeYourself(controller: 'users', action: 'update') {
            before {
                return params.requestor == params.id
            }
        }
    }


}
