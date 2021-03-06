# xiong-jdbc2
## 项目介绍:
    公司dao层使用的是jdbctemplate,但是有很多地方做的不是特别人性化,所以自己根据源码进行了一些改造,纯当练练手.很多地方还做的不是很到位,以后再慢慢优化吧
##     目录结构
    anno    -   注解
    JDBCTemplate    -   基于jdbctemplate拓展类
        params      -   入参解析
        row         -   出参数据每行解析类
        mapping     -   结果映射
        model       -   实体
        sql         -   sql文件解析
    resources       -   配置文件
## 使用介绍 
### 简介:
    1.支持实体,Map,数组入参,实体、Map出参，更加快速开发,不必做多余的非空判断
    2.sql替换更加智能,支持动态替换表名,参数不必与字段对应
    3.大数据反射优化
    4.注解对应数据库字段
    5.
### 数据库操作
1. 添加
```
public static void insert() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★insert★★★★★★★★★★★★★★★★★★★★★★★★");
        TTest t = new TTest();
        t.setUsername("添加数据");
        t.setLove_name("mmm");
        t.setName("你知道?");
        t.setSex("男");
        t.setCreated(new Date());
        t.setTest_id("rrrrrrr");
        t.setTime(new Date());
        Long start = System.currentTimeMillis();
        int i = jdbcTemplate.executeInsert("test.insert", t);
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小:" + i);
    }
```
2. 修改

```
public static void update() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★update★★★★★★★★★★★★★★★★★★★★★★★★");
        TTest t = new TTest(); 
        t.setLove_name("mmm");
        t.setName("你知道?");
        t.setSex("男");
        t.setCreated(new Date());
        t.setTest_id("rrrrrrr");
        t.setTime(new Date());
        t.setId(2);
        Long start = System.currentTimeMillis();
        int i = jdbcTemplate.executeUpdate("test.updateByPrimaryKey", t);
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小:" + i);
    }
```

3. 查询 - 出参为RowSet对象

```
public static void findRowSet() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★findRowSet★★★★★★★★★★★★★★★★★★★★★★★★");
        String sql = " select\n" +
                "      id\n" +
                "      ,username,name,sex,status,created,time,test_id,love_name\n" +
                "      from t_test\n" +
                "      where\n" +
                "      id <= ?";
        Long start = System.currentTimeMillis();
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, "2");
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小:" + sqlRowSet.getString(1));
    }
```

4. 查询 入参为map 出参也是map

```
    public static void findMap() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★findMap★★★★★★★★★★★★★★★★★★★★★★★★");
        Map map = new LinkedHashMap();
        map.put("id", 2);
        map.put("age", 18);
        map.put("ggg", "sssssss");
        Long start = System.currentTimeMillis();
        Map<String, Object> maps = jdbcTemplate.executeQueryMap("test.selectByPrimaryKey", map);
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小:" + maps);
    }
```
5. 查询集合 入参为map 出参为对象

```
 public static void findSqlOjbect() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★findSqlOjbect★★★★★★★★★★★★★★★★★★★★★★★★");
        Object[] ids = new Object[3];
        ids[0] = "2";
        ids[1] = "3307424";
        ids[2] = "3307426";

        Map params = new HashMap();
//        params.put("username", "添加%");
        params.put("table", "t_test");
        params.put("id", ids);
        //params.put("minId", "3307423");
        //params.put("maxId", "3307426");
        Long start = System.currentTimeMillis();
        List<TTest> tTests = jdbcTemplate.executeQueryList("test.operationSql", params, TTest.class);
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小:" + tTests.size());
    }
```
6.查询  入参为对象 出参为对象

```
public static void findListObject() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★findListObject★★★★★★★★★★★★★★★★★★★★★★★★");
        TTest t = new TTest();
        t.setStatus("1");
        Long start = System.currentTimeMillis();
        List<TTest> tTests = jdbcTemplate.executeQueryList("test.selectByExample", t, TTest.class);
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小:" + tTests.size());
    }
```
7. 查询大数据量返回  入参为对象 出参为对象

```
 public static void findBigList() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★findBigList★★★★★★★★★★★★★★★★★★★★★★★★");
        TTest t = new TTest();
        t.setId(2900000);
//        t.setStatus("10");
        Long start = System.currentTimeMillis();
        List<TTest> tTests = jdbcTemplate.executeQueryBigDataList("test.selectByExample", t, TTest.class);
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小:" + tTests.size());
    }
```

#### 配置文件 .sql

```
<?xml version="1.0" encoding="UTF-8" ?>
<sqlGroup name="test">
    <sql id="selectByPrimaryKey">
        select
        a.id,a.username,a.name,a.sex,a.status,a.created,a.time,a.test_id,a.love_name,b.girl_name,b.status as girlStatus
        from t_test a left join t_girl b on a.id = b.id and b.age > :age
        where a.id = :id
    </sql>
    <sql id="selectByExample">
        select
        id
        ,username,name,sex,status,created,time,test_id,love_name
        from t_test
        where
        id > :id and
        and username = :username
        and name = :name
        and sex = :sex
        and status = :status
        and created = :created
        and time = :time
        and test_id = :test_id
        and love_name = :love_name
    </sql>
    <sql id="deleteByKey">
        delete from t_test
        where id = :id
    </sql>
    <!-- 添加 -->
    <sql id="insert">
        insert into t_test (username, name, sex, status, created, time, test_id, love_name)
        values (:username, :name, :sex, :status, :created, :time, :test_id, :love_name)
    </sql>
    <sql id="updateByPrimaryKey">
        update t_test
        set
        username = :username,name = :name,sex = :sex,status = :status,created = :created,time = :time,test_id =
        :test_id,love_name = :love_name
        where
        id = :id
    </sql>
    <!-- 动态替换表名 -->
    <sql id="operationSql">
        select
        a.id,a.username,a.name,a.sex,a.status,a.created,a.time,a.test_id,a.love_name
        from $table a
        where a.username like :username and id in :id
    </sql>
</sqlGroup>
```


#### 出参映射
#####     使用场景:
    当实体和数据库参数不对应的时候,需要进行配对的情况下,需要通过注解进行配对 使用方式:
       
```
    // 数据库字段
    @Column(name="username")
    // 实体字段
    private String loginName;
```
##### 大数据返回
######     当数据量返回特别大时,反射会成为一个巨大的瓶颈,可以查询时间只要几秒,但是反射成对象的时候消耗的也是几秒甚至更多,目前想到的优化方式就是实现某个接口,然后手动对应字段.
    
        
```
//案例:
public class TTest implements ColumnMapping{
 public void mappingColumn(ResultSet rs) throws SQLException {
        // 手动赋值,可以加快赋值速度
 }
}
```

---
###### 1. 使用注意事项 :
######       1. 添加:
        1.添加如果值为null 可以直接传递
######       2.修改:
        set 后面的参数必须存在一个,顺序可以打乱.
        where 后面的参数也必须存在一个,顺序可以打乱
######       3. 查询:
        3.1
            where 后面的参数可以不存在
        3.2
            如果是in like 等函数:
                            1. sql.xml中
                                 1. a.id like :a and a.id in :b
                            2. java 中
                                like 拼接 %a%   in b
        3.3
            如果出现(columnA = :A or columnB = :B) 这种括号条件的话....
            希望能够用表union去做,因为这种参数不太好替换
        3.4
            如果出现参数重名的情况下,比如:status在sql.xml中需要出现多次,并且值是一样的.
            希望能够以第一个status为status1.第二个为status2,然后传参的时候,多传几次...
######       4、动态参数：
            例如动态表名：$table
            参数名称:table
            如果存在动态符号的,则为必填项.
            目前只支持动态表名传参..不支持动态列传参,,因为实在想不到那种场景需要动态传递列名,您可以写两条sql达到目的

###### 2.配置文件定义
    配置文件必须存放在resources的sql目录下,目录级别没有限制..
## 测试
    可以到test.java.test 类里面进行查看,可以加快理解.
    
    如有问题请联系:444368875
