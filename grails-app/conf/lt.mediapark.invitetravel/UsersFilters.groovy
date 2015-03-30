package invitetravel

import lt.mediapark.invitetravel.Picture
import lt.mediapark.invitetravel.User

class UsersFilters {

    def filters = {
        creationMap(controller:'users', action:'*') {
            before = {
                request.JSON.each { k, v ->
                    params[k] = v;
                }
            }
        }
        validateWithUser(controller: 'users', action: 'uploadPhoto') {
            before = {
                def id = params.id
                def user = User.find { it.id.equals(id) }
                return user && user?.pictures < User.MAX_PICTURES
            }
        }
        validatePicture(controller: 'users', action: 'picture') {
            before {
                def id = params.id
                return !!Picture.find { it.id.equals(id) }
            }
        }
    }


}
