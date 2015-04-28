import com.relayrides.pushy.apns.ApnsEnvironment
import com.relayrides.pushy.apns.ApnsPushNotification
import com.relayrides.pushy.apns.PushManager
import com.relayrides.pushy.apns.PushManagerConfiguration
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder
import com.relayrides.pushy.apns.util.SSLContextUtil
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification
import com.relayrides.pushy.apns.util.TokenUtil
import com.restfb.DefaultFacebookClient
import com.restfb.FacebookClient
import com.restfb.Version
import grails.converters.JSON
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import lt.mediapark.invitetravel.UserLevel
import lt.mediapark.invitetravel.enums.UserLevel
import lt.mediapark.invitetravel.enums.UserLevel

import javax.net.ssl.SSLHandshakeException

class BootStrap {

    def grailsApplication
    def grails

    PushManager pushManager


    def init = { servletContext ->



//        JSON.registerObjectMarshaller(new NoClassNameObjectMarshaller())

        def apnsEnv = null;
        grails = grailsApplication.config.grails
        def managerConfig = new PushManagerConfiguration()
        environments {
            development {
                apnsEnv = ApnsEnvironment.getSandboxEnvironment();
            }
            production {
                apnsEnv = ApnsEnvironment.getProductionEnvironment();
            }
        }
        try {
            def sslCtx = SSLContextUtil.createDefaultSSLContext(grails.apns.p12.path,
                    grails.apns.p12.password)
            pushManager = new PushManager<ApnsPushNotification>(
                    apnsEnv,
                    sslCtx,
                    null, //event loop group
                    null, //executor service
                    null, //blocking queue
                    managerConfig,
                    grails.apns.manager.name);

            registerListeners()
            pushManager.start()
        } catch (Exception e) {
            e.printStackTrace()
        }

        grailsApplication.getAllArtefacts().each { klass ->
            addApnsMethods(klass)
            addHTTPMethods(klass)
            klass.metaClass.FB_APP_SECRET = grails.restfb.app.secret
            klass.metaClass.FB_APP_ID = grails.restfb.app.id
            klass.metaClass.PLACES_API_KEY = grails.google.places.api.key

            klass.metaClass.fetchFBObject = { def accessToken, String path, Class resultType, Closure handler ->
                FacebookClient fbClient = new DefaultFacebookClient()
                FacebookClient.AccessToken token = null
                if (accessToken instanceof String) {
                    token = fbClient.obtainExtendedAccessToken(FB_APP_ID, FB_APP_SECRET, accessToken)
                }
                if (accessToken instanceof FacebookClient.AccessToken) {
                    //token expired, needs extending
                    token  = accessToken.expires.time > new Date().time ?:
                            fbClient.obtainExtendedAccessToken(FB_APP_ID, FB_APP_SECRET, accessToken.accessToken)
                }
                if (token == null) {
                    log.error "Provided accessToken ${accessToken} is invalid!"
                } else {
                    log.info "The newly valid token ${token}"
                }
                fbClient = new DefaultFacebookClient(accessToken: token.accessToken, appSecret: FB_APP_SECRET)
                def result = fbClient.fetchObject(path, resultType)
                handler(result)
            }
        }
    }


    def addHTTPMethods(Class klass) {
        klass.metaClass.static.viaHttp = { Method method, String link, Closure responseHandler ->

            def http = new HTTPBuilder(link)
            http.request(method, ContentType.JSON) {

                headers.Accept = 'application/json'

                response.success = { resp, json ->
                    responseHandler(json)
                }
            }
        }

        //curry the httpGet, httpPost, httpDelete, httpPut, httpHead methods
//        Method.values().each { it ->
//            def name = WordUtils.capitalizeFully(it.toString())
//            klass.metaClass.static."http${name}" = klass.metaClass.static.viaHttp.curry(it)
//        }
        klass.metaClass.static.downloadImage = { String address ->
            if (!address) {
                return null
            }
            def tokens = address.tokenize('/')
            String last = tokens[-1]
            def name = last.contains('.')?:"${last}.png"
            def file = new File(name).withOutputStream { out ->
                out << new URL(address).openStream()
            }
            file
        }
    }

    def addApnsMethods(def klass) {
        klass.metaClass.static.apnsManager = pushManager;
        klass.metaClass.static.viaApns = { Closure actions ->
            actions(apnsManager);
        }

        klass.metaClass.static.sendNotification = { token, builder ->

            def tokenBytes = TokenUtil.tokenStringToByteArray(token)

            ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder()
            builder(payloadBuilder)

            def payload = payloadBuilder.buildWithDefaultMaximumLength()

            viaApns { apns ->
                def q = apns.getQueue()
                q.put(new SimpleApnsPushNotification(token, payload))
            }

        }

        //classes with device token saved in them can curry the apns closure
        if (klass.metaClass.hasProperty('deviceToken')) {
            klass.metaClass.registerDeviceToken = { token ->
                klass.metaClass.pushNotification = sendNotification.curry(token)
            }
        }
    }


    def registerListeners = {

        pushManager.registerRejectedNotificationListener { manager, notification, reason ->
            log.error "Notification ${notification} was rejected for reason ${reason}"
        }

        pushManager.registerFailedConnectionListener { manager, cause ->

            if (cause instanceof SSLHandshakeException) {
                //need to shutdown manager since no more SSL
                log.fatal "SSL Certificate expired/invalid (${cause.message})! Shuttind dow push service..."
                pushManager.shutDown()
            }

        }

    }


    def destroy = {
        if (pushManager.isStarted()) pushManager.shutDown()
    }
}
