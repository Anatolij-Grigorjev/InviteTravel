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
import com.restfb.Parameter
import com.restfb.Version
import grails.converters.JSON
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import lt.mediapark.invitetravel.UserLevel
import lt.mediapark.invitetravel.enums.UserLevel
import lt.mediapark.invitetravel.enums.UserLevel
import org.apache.commons.io.FileUtils

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
                String[] objAndParams = path.split('\\?')
                String newPath = objAndParams[0]/* + "&access_token=${accessToken}"*/
                log.debug "Got pure object path: ${newPath}"
//                log.debug " Using app secret ${FB_APP_SECRET}\n" +
//                    " Searching path ${newPath}\n" +
//                    " For class ${resultType}\n"
                def paramsMap = [:]
                if (objAndParams.length > 1) {
                    String params = objAndParams[1]
                    log.debug "Got params: ${params}"
                    String[] nameValuePairs = params.split('=')
                    for (int i = 0; i < nameValuePairs.length; i+=2) {
                        paramsMap[nameValuePairs[i]] = nameValuePairs[i+1]
                    }
//                    log.debug "Got params map: ${paramsMap}"
                }
                def fbClient = new DefaultFacebookClient(accessToken, FB_APP_SECRET, Version.VERSION_2_3)
                def result = null;
                if (paramsMap) {
                    def params = paramsMap.collect { it ->
                        Parameter.with(it.key, it.value)
                    }
                    result = fbClient.fetchObject(newPath, resultType, params.toArray(new Parameter[0]))
                } else {
                    result = fbClient.fetchObject(newPath, resultType)
                }
                log.debug "Fetch result: ${result}"
                handler(result)
                log.debug "Handler done!"
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
            def name = last.contains('.')? last :"${last}.png"
            //this returns an output stream
            def file = new File(name)
            BufferedOutputStream fileStream = file.withOutputStream { out ->
                out << new URL(address).openStream()
            }

            FileUtils.writeByteArrayToFile(file, fileStream.@buf)
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
