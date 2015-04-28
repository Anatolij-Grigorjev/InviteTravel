package lt.mediapark.invitetravel

import com.restfb.DefaultFacebookClient
import com.restfb.FacebookClient
import com.restfb.Version
import com.restfb.types.User as FBUser
import com.restfb.types.User.Picture as FBPicture
import grails.transaction.Transactional
import groovyx.net.http.Method
import lt.mediapark.invitetravel.enums.PlacesResponse
import lt.mediapark.invitetravel.enums.UserLevel

@Transactional
class LoginService {

    def placesService

    Map<?, User> loggedInUsers = [:]

    def loginVK(def jsonMap) {
        //get VK stuff
        finishLogin(user)
    }

    def loginFB(def jsonMap) {
        def accessToken = jsonMap.accessToken
        fetchFBObject(accessToken, 'me', FBUser.class) { FBUser fbUser ->
            def parsedFbId = Long.parseLong(fbUser?.id)
            def user = User.findWhere('userIdFb' : parsedFbId) ?: new User(userIdFb: parsedFbId)
            user.valid = true
            //user is new
            if (!user.id) {
                user.level = UserLevel.findForLevel(jsonMap.level)
                user.name = fbUser?.name
                user.residence = placesService.getPlace(fbUser?.location?.name)
                //fetching profile picture
                fetchFBObject(accessToken, 'me/picture', FBPicture.class) { FBPicture pic ->
                    File avatar = downloadImage(pic?.url)
                    if (avatar) {
                        Picture picture = new Picture(data: avatar.bytes, name: avatar.name, mimeType: 'image/png')
                        picture = picture.save()
                        user.defaultPictureId = picture.id
                    } else {
                        log.warn "User ${parsedFbId} did not have a picture to their profile!"
                    }
                }

            }
            user.pictures << picture
            finishLogin(user)
        }
    }

    private Map finishLogin(User user) {
        def result = [:]
        result.fresh = !!user.id
        if (!user.level) {
            user.level = UserLevel.CANT_PAY
        }
        user.lastActive = new Date()
        user.valid = true
        user.save()
        result.userId = user.id
        loggedInUsers << ["${user.id}" : user]
        result
    }

    def logout(def userId) {
        def user = loggedInUsers.remove(userId)

    }

}
