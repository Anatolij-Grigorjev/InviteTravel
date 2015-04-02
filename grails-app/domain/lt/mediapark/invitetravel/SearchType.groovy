package lt.mediapark.invitetravel

/**
 * Created by anatolij on 02/04/15.
 */
public enum SearchType {

    LIVES_IN(0), WANTS_TO_VISIT(1)

    private int searchNum;

    private SearchType(int num) {
        this.searchNum = num;
    }

    public static SearchType typeFromNum(int num) {
        SearchType.values().find { it.searchNum == num }
    }

}