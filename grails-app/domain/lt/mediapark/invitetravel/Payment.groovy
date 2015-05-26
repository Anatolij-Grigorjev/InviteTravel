package lt.mediapark.invitetravel

import lt.mediapark.invitetravel.constants.SubscriptionType

class Payment {

    static constraints = {
        transactionId nullable: false, unique: true
        subscriptionType nullable: false
    }

    static hasOne = [
            customer: User
    ]

    String transactionId
    User customer
    Date subscriptionStart
    Date subscriptionEnd
    SubscriptionType subscriptionType

    public boolean isValid() {
        return subscriptionEnd? subscriptionEnd >= new Date() : false
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof Payment)) return false

        Payment payment = (Payment) o

        if (customer != payment.customer) return false
        if (subscriptionEnd != payment.subscriptionEnd) return false
        if (subscriptionStart != payment.subscriptionStart) return false
        if (subscriptionType != payment.subscriptionType) return false
        if (transactionId != payment.transactionId) return false

        return true
    }

    int hashCode() {
        int result
        result = (transactionId != null ? transactionId.hashCode() : 0)
        result = 31 * result + (customer != null ? customer.hashCode() : 0)
        result = 31 * result + (subscriptionStart != null ? subscriptionStart.hashCode() : 0)
        result = 31 * result + (subscriptionEnd != null ? subscriptionEnd.hashCode() : 0)
        result = 31 * result + (subscriptionType != null ? subscriptionType.hashCode() : 0)
        return result
    }
}
