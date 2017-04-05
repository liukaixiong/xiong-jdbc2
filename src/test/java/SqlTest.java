/**
 * SQL测试
 *
 * @author Liukx
 * @create 2017-03-23 15:05
 * @email liukx@elab-plus.com
 **/
public class SqlTest {
    public static void columnTest(){
        String sql = " select\n" +
                "        a.id,a.username,a.name,a.sex,a.status as ceshi,a.created,a.time,a.test_id,a.love_name\n" +
                "        from $table a\n" +
                "        where a.username like :username and id in :id   and id = :id";
        int indexOf = sql.indexOf("select");
        System.out.println(indexOf);
    }

    public static void main(String[] args) {
        columnTest();
    }
}
