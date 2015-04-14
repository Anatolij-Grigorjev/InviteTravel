package lt.mediapark.invitetravel

import com.restfb.DefaultFacebookClient
import com.restfb.FacebookClient
import com.restfb.types.User as FBUser
import grails.transaction.Transactional
import lt.mediapark.inviteTravel.enums.PlacesResponse

@Transactional
class LoginService {

    Map<?, User> loggedInUsers = [:]

    def loginVK(def jsonMap) {
        //get VK stuff
        finishLogin(user)
    }

    def loginFB(def jsonMap) {
        FBUser fbUser = fetchMeFb(jsonMap)
        def user = User.findWhere('userIdFb' : fbUser?.id) ?: new User(userIdFb: Long.parse(fbUser?.id))
        user.valid = true
        //user is new
        if (!user.id) {
            user.level = jsonMap.level
            user.name = fbUser?.name
            user.residence = getPlace(fbUser?.location?.name)
            File avatar = downloadImage(fbUser?.picture?.url)
            Picture picture = new Picture(data: avatar.bytes, name: avatar.name, mimeType: 'image/png')
            picture = picture.save()
            user.defaultPictureId = picture.id
        }
        user.pictures << picture
        finishLogin(user)
    }

    private Place getPlace(String name) {
        Place place = null
        if (name) {
            def url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=${PLACES_API_KEY}&types=${'regions'}&name=${name}&language=en"
            httpGet(url) { Map json ->
                log.info("Response code: ${json.status}")
                def code = PlacesResponse.valueOf(json.status)
                switch (code) {
                    case PlacesResponse.OK:
                        List results = json.results
                        if (results) {
                            Map placeMap = results[0]
                            place = new Place(placeId: placeMap."place_id", description: placeMap."name")
                        }
                        break
                    case PlacesResponse.ZERO_RESULTS:
                        log.warn("No results for query ${url}.")
                        break
                    case PlacesResponse.OVER_QUERY_LIMIT:
                        log.warn("Daily query limit reached, try again later.")
                        break
                    case PlacesResponse.REQUEST_DENIED:
                        log.warn("The Places API request was denied, possibly bad key ${PLACES_API_KEY}.")
                        break
                    case PlacesResponse.INVALID_REQUEST:
                        log.warn("The supplied URL ${url} was invalid...")
                        break
                }
            }
        }
        place
    }

    private FBUser fetchMeFb(Map jsonMap) {
        FacebookClient client = new DefaultFacebookClient(accessToken: jsonMap.accessToken, APP_SECRET)
        client.fetchObject('me', FBUser.class)
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
