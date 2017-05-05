package model;

import anno.Column;

import java.sql.SQLException;
import java.util.Date;
//implements ColumnMapping
//@Table(name ="t_test")
public class TTest1 {
    //
    // 表字段 : t_test.id
    @Column(name="id")
    private Integer ids;

    //
    // 表字段 : t_test.username
    @Column(name="username")
    private String loginName;

    //
    // 表字段 : t_test.name
    @Column(name="name")
    private String realName;

    //
    // 表字段 : t_test.sex
    @Column(name="sex")
    private String sex1;

    //
    // 表字段 : t_test.status
    @Column(name="status")
    private String status1;

    //
    // 表字段 : t_test.time
    @Column(name="time")
    private Date time1;

    //
    // 表字段 : t_test.created
    @Column(name="created")
    private Date created1;

    //
    // 表字段 : t_test.test_id
//    @Column(name="test_id")
    private String testId;

    //
    // 表字段 : t_test.love_name
    @Column(name="love_name")
    private String loveName;
    @Column(name="girl_name")
    private String girlName;
    @Column(name="girlStatus")
    private String girlStatus;

    public Integer getIds() {
        return ids;
    }

    public void setIds(Integer ids) {
        this.ids = ids;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getSex1() {
        return sex1;
    }

    public void setSex1(String sex1) {
        this.sex1 = sex1;
    }

    public String getStatus1() {
        return status1;
    }

    public void setStatus1(String status1) {
        this.status1 = status1;
    }

    public Date getTime1() {
        return time1;
    }

    public void setTime1(Date time1) {
        this.time1 = time1;
    }

    public Date getCreated1() {
        return created1;
    }

    public void setCreated1(Date created1) {
        this.created1 = created1;
    }


    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getLoveName() {
        return loveName;
    }

    public void setLoveName(String loveName) {
        this.loveName = loveName;
    }

    public String getGirlName() {
        return girlName;
    }

    public void setGirlName(String girlName) {
        this.girlName = girlName;
    }

    public String getGirlStatus() {
        return girlStatus;
    }

    public void setGirlStatus(String girlStatus) {
        this.girlStatus = girlStatus;
    }

    /**
     * 对应映射sql中的数据
     *
     * @param rs
     * @throws SQLException
     */
//    public void mappingColumn(ResultSet rs) throws SQLException {
//        this.setId(rs.getInt("id"));
//        this.setLove_name(rs.getString("love_name"));
//        this.setName(rs.getString("name"));
//        this.setUsername(rs.getString("username"));
//        this.setCreated(rs.getDate("created"));
//        this.setTime(rs.getDate("time"));
//        this.setSex(rs.getString("sex"));
//        this.setStatus(rs.getString("status"));
//        this.setTest_id(rs.getString("test_id"));
//    }
}