package lt.mediapark.invitetravel

/**
 * Created by anatolij on 27/03/15.
 */
enum UserLevel {

    CAN_PAY_FOR_TWO(3), CAN_PAY_FOR_ONE(2), CANT_PAY(1);

    private UserLevel(int rank) {
        this.rank = rank
    }

    public boolean canTalkTo(UserLevel level) {
        this.rank >= level?.rank
    }

}
