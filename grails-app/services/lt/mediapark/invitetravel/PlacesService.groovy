package lt.mediapark.invitetravel

import grails.transaction.Transactional
import groovyx.net.http.Method
import lt.mediapark.invitetravel.enums.PlacesResponse

@Transactional
class PlacesService {

    /**
     * Fetches a place by query string using the Google Places API.
     * Persists a found place into the DB if it was not there before
     * @param name the query
     * @return the (new) place
     */
    def Place getPlace(String name) {
        Place place = null
        if (name) {
            def url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=${name}&types=(regions)&language=en_US&key=${PLACES_API_KEY}"
            viaHttp(Method.GET, url) { Map json ->
                log.info("Response code: ${json.status}")
                def code = PlacesResponse.valueOf(json.status)
                switch (code) {
                    case PlacesResponse.OK:
                        List results = json.predictions
                        if (results) {
                            Map placeMap = results[0]
                            place = Place.findOrSaveWhere(placeId: placeMap.place_id, description: placeMap.description)
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
        log.debug "Resolved name ${name} to place ${place?.description}"
        place
    }

}
