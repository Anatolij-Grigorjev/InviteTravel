package lt.mediapark.invitetravel.constants

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

    public static UserLevel findForLevel(def level) {
        if (level == null) {
            return null
        }
        if (level instanceof Integer)
            return values().find { it.rank == level }
        if (level instanceof String)
            return findForLevel(Integer.parseInt(level))
        return findForLevel(level.toString())
    }
}
