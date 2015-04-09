package lt.mediapark.invitetravel

import com.restfb.DefaultFacebookClient
import com.restfb.FacebookClient
import grails.transaction.Transactional
import com.restfb.types.User as FbUser
import grails.util.Holders

@Transactional
class UsersService {

    Map<?, User> loggedInUsers = [:]

    def loginVK(def jsonMap) {
        //get VK stuff
        finishLogin(user)
    }

    def loginFB(def jsonMap) {
        FbUser fbUser = fetchMeFb(jsonMap)
        def user = User.findWhere('userIdFb' : fbUser?.id) ?: new User(userIdFb: Long.parse(fbUser?.id))
        user.userValid = true
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
                    "${PLACES_API_KEY}&types=${'regions'}&name=${name}"
        } else {
            return null;
        }
    }

    private FbUser fetchMeFb(Map jsonMap) {
        FacebookClient client = new DefaultFacebookClient(accessToken: jsonMap.accessToken, APP_SECRET)
        client.fetchObject('me', FbUser.class)
    }

    private Map finishLogin(User user) {
        def result = [:]
        result.fresh = !!user.id
        user.lastActive = new Date()
        user.userValid = true
        user.save()
        result.userId = user.id
        loggedInUsers << ["${user.id}" : user]
        result
    }

    def updateUser(def userId, def jsonMap) {
        def user = User.get(userId)

        jsonMap.keys.each { it ->
            if (!it.eqauls('id') && !it.equals('name')) {
                if (user.$it) {
                    user.$it = jsonMap.$it
                }
            }
        }

        user.save()
    }

    def logout(def userId) {
        def user = loggedInUsers.remove(userId)

    }

    def getUsersList(def userId, def amount, def jsonMap) {
        if (jsonMap?.fresh) {
            loggedInUsers[userId]?.listedIds?.clear()
        }
        User.createCriteria().list {
            if (jsonMap?.searchType == 0) {
                residence {
                    if (jsonMap?.query) {
                        like('description', "%${jsonMap.query}%")
                    }
                    if (jsonMap?.place) {
                        like('placeId', "${jsonMap.place?.placeId}")
                        like('description', "%${jsonMap.place?.description}%")
                    }
                }
            }
            if (jsonMap?.searchType == 1) {
                wantToVisit {
                    if (jsonMap?.query) {
                        like('description', "%${jsonMap.query}%")
                    }
                    if (jsonMap?.place) {
                        like('placeId', "${jsonMap.place?.placeId}")
                        like('description', "%${jsonMap.place?.description}%")
                    }
                }
            }
            order('lastActive', 'desc')
            maxResults(amount)
            eq(userValid, true)
            notIn('id') { loggedInUsers[userId]?.listedIds }
        }
    }

    def boolean userReady(def userId) {
        def user = loggedInUsers[userId]
        user?.lastActive = new Date()
        user && User.exists(userId)
    }

    def User getUser(def userId) {
        loggedInUsers[userId]?: User.findWhere(userValid: true, id: Long.parseLong(userId));
    }
}
