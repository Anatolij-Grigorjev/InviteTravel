import com.relayrides.pushy.apns.ApnsEnvironment
import com.relayrides.pushy.apns.ApnsPushNotification
import com.relayrides.pushy.apns.PushManager
import com.relayrides.pushy.apns.PushManagerConfiguration
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder
import com.relayrides.pushy.apns.util.SSLContextUtil
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification
import com.relayrides.pushy.apns.util.TokenUtil
import com.restfb.DefaultFacebookClient
import com.restfb.Parameter
import com.restfb.Version
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import lt.mediapark.invitetravel.constants.SysConst

import javax.net.ssl.SSLHandshakeException
import java.util.concurrent.atomic.AtomicLong

class BootStrap {

    def grailsApplication
    def grails

    PushManager pushManager

    AtomicLong lng = new AtomicLong(1)

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
            def sslCtx = SSLContextUtil.createDefaultSSLContext((String)grails.apns.p12.path, (String)grails.apns.p12.password)
            pushManager = new PushManager<ApnsPushNotification>(
                    apnsEnv,
                    sslCtx,
                    null, //event loop group
                    null, //executor service
                    null, //blocking queue
                    managerConfig,
                    (String)grails.apns.manager.name);
            registerListeners()
            pushManager.start()
        } catch (Exception e) {
            e.printStackTrace()
        }
        //system constants
        SysConst.init(grails)

        grailsApplication.getAllArtefacts().each { klass ->
            addApnsMethods(klass)
            addHTTPMethods(klass)

            klass.metaClass.fetchFBObject = { String accessToken, String path, Class resultType, Closure handler ->
                String[] objAndParams = path.split('\\?')
                String newPath = objAndParams[0]
                log.debug "Got pure object path: ${newPath}"
                def paramsMap = [:]
                if (objAndParams.length > 1) {
                    String params = objAndParams[1]
                    log.debug "Got params: ${params}"
                    def paramPairs = params.split('&')
                    paramsMap = paramPairs.collectEntries { pair ->
                        String[] nameValuePair = pair.split('=')
                        [(nameValuePair[0]) : nameValuePair[1]]
                    }

                    log.debug "Got params map: ${paramsMap}"
                }
                def fbClient = new DefaultFacebookClient(accessToken, SysConst.FB_APP_SECRET, Version.VERSION_2_3)
                def result;
                if (paramsMap) {
                    def params = paramsMap.collect { it ->
                        Parameter.with(it.key?.toString(), it.value)
                    }
                    result = fbClient.fetchObject(newPath, resultType, params.toArray(new Parameter[0]))
                } else {
                    result = fbClient.fetchObject(newPath, resultType)
                }
                log.debug "Fetch result: ${result}"
                handler(result)
            }
        }
    }

    def addHTTPMethods(Class klass) {
        klass.metaClass.static.viaHttpGet = { String link, ContentType type = ContentType.JSON, Closure responseHandler ->
            def http = new HTTPBuilder(link)
            http.request(Method.GET, type) {
                response.success = { resp, json ->
                    responseHandler(json)
                }
            }
        }
//        klass.metaClass.static.httpPostJson = { String link, def theJson, ContentType type = ContentType.JSON, Closure responseHandler ->
//            def http = new HTTPBuilder(link)
//            http.headers.'Accept-Encoding' = 'gzip,deflate,sdch'
//            http.headers.'Content-Type' = 'text/plain;charset=UTF-8'
//            http.post(body: theBody/*, requestContentType: type*/) { response, json ->
//                    log.info("Status: ${response.statusLine}")
//                    log.info("All of it: ${response.dump()}")
//                    responseHandler(json)
//            }
//        }

        //curry the httpGet, httpPost, httpDelete, httpPut, httpHead methods
//        Method.values().each { it ->
//            def name = WordUtils.capitalizeFully(it.toString())
//            klass.metaClass.static."http${name}" = klass.metaClass.static.viaHttp.curry(it)
//        }
        klass.metaClass.static.downloadImage = { String address ->
            if (!address) {
                return null
            }
            def name = "image-${lng.incrementAndGet()}.png"
            //this returns an output stream
            def file = File.createTempFile(name, '')
            file.bytes = new URL(address).bytes
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
                log.fatal "SSL Certificate expired/invalid (${cause.message})! Shutting down push service..."
                pushManager.shutDown()
            }

        }

    }


    def destroy = {
        if (pushManager?.isStarted()) pushManager.shutDown()
    }
}
