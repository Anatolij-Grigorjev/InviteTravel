package lt.mediapark.invitetravel

import grails.converters.JSON
import lt.mediapark.invitetravel.constants.SysConst

class SubscriptionController {

    def subscriptionService
    def JSONConversionService
    def chatService

    static allowedMethods = [
            extend: 'POST'
    ]

    def extend = {
        User user = params.currUser
        //sending the right JSON object
        String payload = request.JSON.payload
        boolean allGood = subscriptionService.updateUserSubscription(payload, user, SysConst.APPLE_PAYMENT_LINK)
        chatService.refreshUserMessages(user)
        if (allGood) {
            Map resultMap = JSONConversionService.paymentsMap(user.payments)
            render resultMap as JSON
        } else {
            render 501 as JSON
        }
    }
}
