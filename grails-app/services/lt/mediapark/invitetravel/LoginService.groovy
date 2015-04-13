package lt.mediapark.invitetravel

import com.restfb.DefaultFacebookClient
import com.restfb.FacebookClient
import grails.transaction.Transactional

@Transactional
class LoginService {

    Map<?, User> loggedInUsers = [:]

    def loginVK(def jsonMap) {
        //get VK stuff
        finishLogin(user)
    }

    def loginFB(def jsonMap) {
        com.restfb.types.User fbUser = fetchMeFb(jsonMap)
        def user = User.findWhere('userIdFb' : fbUser?.id) ?: new User(userIdFb: Long.parse(fbUser?.id))
        user.valid = true
        if (!user.id) {
            user.level = jsonMap.level
        }
        user.name = fbUser?.name
        user.residence = getPlace(fbUser?.location?.name)
        finishLogin(user)
    }

    private Place getPlace(String name) {
        if (name) {
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=" +
                    "${PLACES_API_KEY}&types=${'regions'}&name=${name}&language=en"
        } else {
            return null;
        }
    }

    private com.restfb.types.User fetchMeFb(Map jsonMap) {
        FacebookClient client = new DefaultFacebookClient(accessToken: jsonMap.accessToken, APP_SECRET)
        client.fetchObject('me', com.restfb.types.User.class)
    }

    private Map finishLogin(User user) {
        def result = [:]
        result.fresh = !!user.id
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
