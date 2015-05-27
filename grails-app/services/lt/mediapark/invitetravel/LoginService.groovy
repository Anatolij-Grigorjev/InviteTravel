package lt.mediapark.invitetravel

import com.restfb.types.User as FBUser
import grails.transaction.Transactional
import lt.mediapark.invitetravel.constants.UserLevel

@Transactional
class LoginService {

    def placesService

    Set<Long> loggedInUsers = Collections.synchronizedSet([] as Set)

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
                user.level = UserLevel.findForLevel(jsonMap.level)
                user.name = fbUser?.name
                if (fbUser?.location) {
                    user.residence = placesService.getPlace(fbUser?.location?.name)
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
                fetchFBObject(accessToken, 'me/picture?redirect=false&width=320&height=320', FacebookPictureHolder.class) { FacebookPictureHolder holder ->
                    FacebookPicture fbPicture = holder.data
                    File avatar = downloadImage(fbPicture?.url)
                    if (avatar) {
                        Picture picture = new Picture(data: avatar.bytes, name: avatar.name, mimeType: 'image/png', index: 0)
                        picture = picture.save()
                        user.pictures << picture
                    } else {
                        log.warn "User ${parsedFbId} did not have a picture to their profile!"
                    }
                }
                user = user.save()
            }
        } else if (!user.id) {
            //user doesn't exist AND no level supplied
            return Collections.EMPTY_MAP
        }
        finishLogin(user)
    }

    private Map finishLogin(User user) {
        def result = [:]
        if (!user.level) {
            user.level = UserLevel.CANT_PAY
        }
        user.lastActive = new Date()
        user.valid = true
        user = user.save(flush: true)
        result.userId = user.id
        user = user.refresh()
        log.info "Logging in user ${user}"
        loggedInUsers << user.id
        result
    }

    def logout(def userId) {
        loggedInUsers.remove(userId)
    }

}
