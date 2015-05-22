package lt.mediapark.invitetravel

import grails.converters.JSON
import lt.mediapark.invitetravel.utils.ConversionsHelper
import org.json.simple.JSONObject
import org.springframework.web.multipart.commons.CommonsMultipartFile

class SubscriptionController {

    def subscriptionService
    def usersService


    static allowedMethods = [
            extend: 'POST'
    ]

    def extend = {
        Long userId = Long.parseLong(params.requestor)
        User user = usersService.get(userId)
        //sending the right JSON object
        String payload = request.JSON.payload
        boolean allGood = subscriptionService.updateUserSubscription(payload, user, APPLE_PAYMENT_LINK)
        if (allGood) {
            Map resultMap = ConversionsHelper.paymentsMap(user.payments)
            render resultMap as JSON
        } else {
            render 501 as JSON
        }
    }
}