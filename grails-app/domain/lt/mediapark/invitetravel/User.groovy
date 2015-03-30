package lt.mediapark.invitetravel

class User {

    public static final int MAX_PICTURES = 4;

    static constraints = {
        id nullable: false, unique: true
        name nullable: false
        level nullable: false
        deviceToken nullable: false, unique: true
        lastPayment nullable: false
        unreadMessages min: 0, nullable: false
    }

    def id
    def name
    def description
    def residence
    def deviceToken
    List wantToVisit
    UserLevel level
    List<Picture> pictures = []
    Date lastPayment
    Picture defaultPicture
    int unreadMessages
}
