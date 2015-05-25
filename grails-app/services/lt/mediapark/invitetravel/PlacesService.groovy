package lt.mediapark.invitetravel

import grails.transaction.Transactional
import groovy.transform.Synchronized
import groovy.transform.WithWriteLock
import lt.mediapark.invitetravel.enums.PlacesResponse
import org.hibernate.Session
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation

import java.util.concurrent.locks.ReentrantLock

@Transactional
class PlacesService {

    /**
     * Fetches a place by query string using the Google Places API.
     * Persists a found place into the DB if it was not there before
     * @param name the query
     * @return the (new) place
     */
    @WithWriteLock
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    def Place getPlace(String name) {
        Place place = null
        if (name) {
            def url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=${name}&types=(regions)&language=en_US&key=${PLACES_API_KEY}"
            viaHttpGet(url) { Map json ->
                log.info("Response code: ${json.status}")
                def code = PlacesResponse.valueOf(json.status)
                switch (code) {
                    case PlacesResponse.OK:
                        List results = json.predictions
                        if (results) {
                            Map placeMap = results[0]
                            try {
                                place = Place.findOrSaveWhere(placeId: placeMap.place_id, description: placeMap.description)
                            } catch (e) {
                                log.error(e.message, e)
                                Place.withNewTransaction {
                                    place = Place.findOrCreateByPlaceId(placeMap.place_id?.toString())
                                    if (!place.id || !place.description) {
                                        place.description = placeMap.description
                                        try {
                                            place = place.save()
                                        } catch (ex) {
                                            log.error(ex.message, ex)
                                            Place.withNewTransaction {
                                                place = Place.findByPlaceId(placeMap.place_id?.toString())
                                            }
                                        }
                                    }
                                }
                            }
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
