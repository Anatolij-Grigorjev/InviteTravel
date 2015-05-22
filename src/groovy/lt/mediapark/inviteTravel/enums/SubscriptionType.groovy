package lt.mediapark.invitetravel.enums
import static lt.mediapark.invitetravel.enums.UserLevel.*;

/**
 * Created by anatolij on 18/05/15.
 */
public enum SubscriptionType {

    ONE_PERSON_1_MONTH("com.nabaka.fly.1person1month", CAN_PAY_FOR_ONE, 1),
    ONE_PERSON_3_MONTHS("com.nabaka.fly.1person3month", CAN_PAY_FOR_ONE, 3),
    ONE_PERSON_12_MONTHS("com.nabaka.fly.1person12month", CAN_PAY_FOR_ONE, 12),
    TWO_PEOPLE_1_MONTH("com.nabaka.fly.2people1month", CAN_PAY_FOR_TWO, 1),
    TWO_PEOPLE_3_MONTHS("com.nabaka.fly.2people3month", CAN_PAY_FOR_TWO, 3),
    TWO_PEOPLE_12_MONTHS("com.nabaka.fly.2people12month", CAN_PAY_FOR_TWO, 12)

    private String id
    private UserLevel subLevel
    private int months

    private SubscriptionType(String id, UserLevel level, int months) {
        this.id = id;
        this.subLevel = level
        this.months = months
    }

    public static SubscriptionType getById(String id) {
        return values().find {it.id.equals(id)}
    }

    public static List<SubscriptionType> forLevel(UserLevel level) {
        return values().findAll {it.subLevel.equals(level)}
    }

    public String getId() {
        return id
    }

    public UserLevel getSubLevel() {
        return subLevel
    }

    public int getMonths() {
        return months
    }

    public String toString() {
        return id
    }
}
