package lt.mediapark.invitetravel

import grails.converters.JSON
import lt.mediapark.invitetravel.constants.SysConst
import lt.mediapark.invitetravel.utils.ConversionsHelper

class SubscriptionController {

    def subscriptionService
    def usersService
    def chatService

    static allowedMethods = [
            extend: 'POST'
    ]

    def extend = {
        Long userId = Long.parseLong(params.requestor)
        User user = usersService.get(userId)
        //sending the right JSON object
        String payload = request.JSON.payload
        boolean allGood = subscriptionService.updateUserSubscription(payload, user, SysConst.APPLE_PAYMENT_LINK)
        chatService.refreshUserMessages(user)
        if (allGood) {
            Map resultMap = ConversionsHelper.paymentsMap(user.payments)
            render resultMap as JSON
        } else {
            render 501 as JSON
        }
    }
}
