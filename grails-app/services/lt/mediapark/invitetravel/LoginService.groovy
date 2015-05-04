package lt.mediapark.invitetravel

import com.restfb.Facebook
import com.restfb.types.User as FBUser

import grails.transaction.Transactional
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
        def parsedFbId = Long.parseLong(jsonMap.userId)
        def user = User.findWhere('userIdFb' : parsedFbId) ?: new User(userIdFb: parsedFbId)
        user.valid = true
        //user is new
        if (!user.id && jsonMap.level) {
            fetchFBObject(accessToken, 'me?fields=locale,location,name,id', FBUser.class) { FBUser fbUser ->
                user.level = UserLevel.findForLevel(Integer.parseInt(jsonMap.level))
                user.name = fbUser?.name
                if (fbUser?.location) {
                    user.residence = placesService.getPlace(fbUser?.location.name)
                } else {
                    log.warn("No location disclosed, attempting to resolve via locale ${fbUser?.locale}...")
                    try {
                        String[] split = fbUser?.locale?.split('_')
                        Locale locale
                        if (split.size() > 1) {
                            locale = new Locale(split[0], split[1])
                        } else {
                            locale = new Locale(split[0])
                        }
                        user.residence = placesService.getPlace(locale.displayCountry)
                    } catch (Exception e) {
                        log.error("Could not achieve residence via locale! ${e.message}", e)
                    }
                }
                //fetching profile picture
                fetchFBObject(accessToken, 'me/picture?redirect=false&width=320&height=320', FacebookPicture.class) { FacebookPicture fbPicture ->
                    File avatar = downloadImage(fbPicture?.url)
                    if (avatar) {
                        Picture picture = new Picture(data: avatar.bytes, name: avatar.name, mimeType: 'image/png')
                        picture = picture.save()
                        user.pictures << picture
                        if (!user.defaultPictureId) user.defaultPictureId = picture.id
                    } else {
                        log.warn "User ${parsedFbId} did not have a picture to their profile!"
                    }
                }
            }
        } else if (!user.id) {
            //user doesnt exist AND no level supplied
            return Collections.EMPTY_MAP
        }
        finishLogin(user)
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
        log.info "Logging in user ${user}"
        loggedInUsers << [(user.id) : user]
        result
    }

    def logout(def userId) {
        def user = loggedInUsers.remove(userId)

    }

}
