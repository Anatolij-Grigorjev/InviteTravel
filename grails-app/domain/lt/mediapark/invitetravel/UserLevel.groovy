package lt.mediapark.invitetravel

/**
 * Created by anatolij on 27/03/15.
 */
enum UserLevel {

    CAN_PAY_FOR_TWO(2), CAN_PAY_FOR_ONE(1), CANT_PAY(0);

    private int rank

    private UserLevel(int rank) {
        this.rank = rank
    }

    public boolean canTalkTo(UserLevel level) {
        this.rank >= level?.rank
    }

    public static UserLevel findForLevel(int level) {
        UserLevel.find { it.rank == level }
    }

}
