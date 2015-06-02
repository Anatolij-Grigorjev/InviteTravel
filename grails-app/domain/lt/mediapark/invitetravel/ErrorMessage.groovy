package lt.mediapark.invitetravel

import lt.mediapark.invitetravel.constants.ErrorType

/**
 * Created by anatolij on 28/05/15.
 */
class ErrorMessage {

    static constraints = {
        type nullable: false
    }

//    static hasMany = [
//        'solutions' : UserLevel
//    ]

    static belongsTo = [
        'message' : ChatMessage
    ]

    ErrorType type
    String solutions

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof ErrorMessage)) return false

        ErrorMessage that = (ErrorMessage) o

        if (solutions != that.solutions) return false
        if (type != that.type) return false

        return true
    }

    int hashCode() {
        int result
        result = type.hashCode()
        result = 31 * result + (solutions != null ? solutions.hashCode() : 0)
        return result
    }
}
