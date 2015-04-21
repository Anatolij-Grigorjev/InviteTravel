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
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.WordUtils
import org.jsoup.Connection

import javax.net.ssl.SSLHandshakeException


class BootStrap {

    def grailsApplication
    def grails

    PushManager pushManager

    def init = { servletContext ->
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
            klass.metaClass.APP_SECRET = grails.restfb.app.secret
            klass.metaClass.PLACES_API_KEY = grails.google.places.api.key
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
