package lt.mediapark.invitetravel

import grails.transaction.Transactional
import groovy.json.JsonSlurper
import lt.mediapark.invitetravel.constants.SubscriptionType
import lt.mediapark.invitetravel.constants.SysConst
import org.json.simple.JSONObject

@Transactional
class SubscriptionService {

    def MILLIS_IN_A_DAY = 1000 * 3600 * 24;

    def updateUserSubscription(String payload, User user, String url) {
            def json = sendSubscriptionReceiptValidation(payload, url)
            log.info("Subscription returns: ${json}")
            switch (json.status) {
                case 0:
                    Calendar c = Calendar.getInstance()
                    List<Map> purchases = json.receipt.in_app
                    def transactionIds = Payment.all.transactionId
                    //reduce list of pruchases to those not yet processed
                    purchases = purchases.findAll { !transactionIds.contains(it.transaction_id) }
                    //ascending order of purchase date
                    //to naturally merge the map and contain only most recent purchases
                    purchases.sort(true) { it.original_purchase_date_ms }
                    def purchasesMap = purchases.collectEntries { [(it.product_id) : it] }
                    purchasesMap.each { entry ->
                        long initialMillis = Long.parseLong(entry.value.original_purchase_date_ms)
                        long startOfDay = initialMillis - (initialMillis % MILLIS_IN_A_DAY)
                        def subStart = new Date(startOfDay)
                        SubscriptionType type = SubscriptionType.getById(String.valueOf(entry.key))
                        c.setTime(subStart)
                        c.add(Calendar.MONTH, type.months)
                        def subEnd = c.getTime()
                        def payment = new Payment(
                                customer: user
                                , transactionId: entry.value.transaction_id
                                , subscriptionType: type
                                , subscriptionStart: subStart
                                , subscriptionEnd: subEnd
                        )
                        payment.save()
                        //merge into user payments list
                        if (user.payments[(type.id)]) {
                            //this type has an active payment, need to merge the dates
                            Date prevEnd = user.payments[(type.id)].subscriptionEnd
                            Long diff = (payment.subscriptionEnd.time > prevEnd.time) ? payment.subscriptionEnd.time - prevEnd.time : 0
                            user.payments[(type.id)].subscriptionEnd = new Date(prevEnd.time + diff)
                        } else {
                            user.payments << [(type.id) : payment]
                        }
                    }
                    user.refresh()
                    return true
                case 21007:
                    //this code means its a sandbox purchase but was sent to prod URL, needs rerouting
                    log.info('Production link used for sandbox purchase! rerouting...')
                    return updateUserSubscription(payload, user, SysConst.APPLE_PAYMENT_LINK_DEBUG)
                default:
                    //other codes are bad
                    log.error("Something went terribly wrong while processing the receipt! " +
                            "The status we got was ${json.status}\nHere is the payload: ${payload}" +
                            "\nHere is the link: ${url}\nThis was the user: ${user}\n" +
                            "This is the entire response: ${json.dump()}")
                    return false;
            }
    }


    private def sendSubscriptionReceiptValidation(String payload, String url) {
        URL object=new URL(url);
        HttpURLConnection con = (HttpURLConnection) object.openConnection();
        con.doOutput = true
        con.doInput = true
        con.setRequestProperty("Content-Type", "text/html;charset=UTF-8");
        con.setRequestProperty("Accept", "application/json");
        con.requestMethod = "POST";
        JSONObject jsonPayload = new JSONObject()
        jsonPayload['receipt-data'] = payload
        OutputStreamWriter wr= new OutputStreamWriter(con.outputStream);
        wr.write(jsonPayload.toString());
        wr.flush();

        //display what returns the POST request
        StringBuilder sb = new StringBuilder();
        if (con.responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader(con.inputStream,"UTF-8"));
            def line;
            while ((line = br.readLine())) {
                sb.append(line + "\n");
            }
            br.close();

            return new JsonSlurper().parseText(sb.toString())

        } else {
            log.error(con.getResponseMessage());
        }
    }
}
