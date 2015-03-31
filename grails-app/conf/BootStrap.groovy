import com.relayrides.pushy.apns.ApnsEnvironment
import com.relayrides.pushy.apns.ApnsPushNotification
import com.relayrides.pushy.apns.PushManager
import com.relayrides.pushy.apns.PushManagerConfiguration
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder
import com.relayrides.pushy.apns.util.SSLContextUtil
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification
import com.relayrides.pushy.apns.util.TokenUtil

import javax.net.ssl.SSLHandshakeException

class BootStrap {

    def grailsApplication
    PushManager pushManager

    def init = { servletContext ->
        def apnsEnv = null;
        def managerConfig = new PushManagerConfiguration()
        environments {
            development {
                apnsEnv = ApnsEnvironment.getSandboxEnvironment();
            }
            production {
                apnsEnv = ApnsEnvironment.getProductionEnvironment();
            }
        }
        def sslCtx = SSLContextUtil.createDefaultSSLContext(grailsApplication.apns.p12.path,
                grailsApplication.apns.p12.password)
        pushManager = new PushManager<ApnsPushNotification>(
                apnsEnv,
                sslCtx,
                null, //even loop group
                null, //executor service
                null, //blocking queue
                managerConfig,
                grailsApplication.apns.manager.name);

        grailsApplication.getAllArtefacts().each { klass ->
            klass.metaClass.static.apnsManager = manager;
            klass.metaClass.static.viaApns = { Closure actions ->
                actions(manager);
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
            if (klass.getDeclaredField('deviceToken')) {
                klass.metaClass.registerDeviceToken = { token ->
                    klass.metaClass.pushNotification = sendNotification.curry(token)
                }
            }

        }
        registerListeners()
        pushManager.start()
    }


    def registerListeners = {

        pushManager.registerRejectedNotificationListener { manager, notification, reason ->
            log.error "Notification ${notification} was rejected for reason ${reason}"
        }

        pushManager.registerFailedConnectionListener { manager, cause ->

            if (cause instanceof SSLHandshakeException) {
                //need to shutdown manager since no more SSL
                log.fatal "SSL Certificate expired/invalid (${cause.message})! Shuttind dow push service..."
                pushManager.shutDown
            }

        }

    }


    def destroy = {
        if (pushManager.isStarted()) pushManager.shutDown
    }
}
