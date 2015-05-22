package lt.mediapark.invitetravel.enums

/**
 * Created by anatolij on 27/03/15.
 */
enum UserLevel {

    CAN_PAY_FOR_TWO(2), CAN_PAY_FOR_ONE(1), CANT_PAY(0);

    private static Map<UserLevel, List<UserLevel>> talkMatrix = [
            (CANT_PAY): [CAN_PAY_FOR_TWO],
            (CAN_PAY_FOR_ONE): [CAN_PAY_FOR_ONE, CAN_PAY_FOR_TWO],
            (CAN_PAY_FOR_TWO): values() as List
    ]

    private int rank

    public int getRank() {
        rank
    }

    private UserLevel(int rank) {
        this.rank = rank
    }

    public boolean canTalkTo(UserLevel level) {
        return talkMatrix[(this)].contains(level)
    }

    public static UserLevel findForLevel(int level) {
        values().find { it.rank == level }
    }
}
