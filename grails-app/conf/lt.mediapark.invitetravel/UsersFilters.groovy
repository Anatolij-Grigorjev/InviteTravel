package invitetravel

import lt.mediapark.invitetravel.Picture
import lt.mediapark.invitetravel.User

class UsersFilters {

    def filters = {
        validateWithUser(controller: 'users', action: 'uploadPhoto|update') {
            before = {
                def id = params.id
                return User.exists(id)
            }
        }
        validatePicture(controller: 'users', action: 'picture') {
            before {
                def id = params.id
                return !!Picture.exists(id)
            }
        }
    }


}
