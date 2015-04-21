package lt.mediapark.invitetravel.enums

/**
 * Created by anatolij on 27/03/15.
 */
enum UserLevel {

    CAN_PAY_FOR_TWO(2), CAN_PAY_FOR_ONE(1), CANT_PAY(0);

    private int rank

    public int getRank() {
        rank
    }

    private UserLevel(int rank) {
        this.rank = rank
    }

    public boolean canTalkTo(UserLevel level) {
        this.rank >= level?.rank
    }

    public static UserLevel findForLevel(int level) {
        UserLevel.values().find { it.rank == level }
    }

}
