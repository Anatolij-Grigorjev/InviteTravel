package lt.mediapark.invitetravel.constants

import groovy.transform.CompileStatic

/**
 * Created by anatolij on 25/05/15.
 */
class SysConst {

    public static String FB_APP_SECRET
    public static String FB_APP_ID
    public static String PLACES_API_KEY
    public static String APPLE_PAYMENT_LINK
    public static String APPLE_PAYMENT_LINK_DEBUG

    public static void init(Map grails) {
        FB_APP_SECRET = grails.restfb.app.secret
        FB_APP_ID = grails.restfb.app.id
        PLACES_API_KEY = grails.google.places.api.key
        APPLE_PAYMENT_LINK = grails.apple.subscription.pay
        APPLE_PAYMENT_LINK_DEBUG = grails.apple.subscription.sandbox
    }

}
