package model;

import mapping.ColumnMapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class TTest implements ColumnMapping {
    //
    // 表字段 : t_test.id
    private Integer id;

    //
    // 表字段 : t_test.username
    private String username;

    //
    // 表字段 : t_test.name
    private String name;

    //
    // 表字段 : t_test.sex
    private String sex;

    //
    // 表字段 : t_test.status
    private String status;

    //
    // 表字段 : t_test.time
    private Date time;

    //
    // 表字段 : t_test.created
    private Date created;

    //
    // 表字段 : t_test.test_id
    private String test_id;

    //
    // 表字段 : t_test.love_name
    private String love_name;

    private String girl_name;

    private String girlStatus;

    public String getGirl_name() {
        return girl_name;
    }

    public void setGirl_name(String girl_name) {
        this.girl_name = girl_name;
    }

    public String getGirlStatus() {
        return girlStatus;
    }

    public void setGirlStatus(String girlStatus) {
        this.girlStatus = girlStatus;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_test.id
     *
     * @mbggenerated
     */
    public Integer getId() {
        return id;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_test.id
     *
     * @mbggenerated
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_test.username
     *
     * @mbggenerated
     */
    public String getUsername() {
        return username;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_test.username
     *
     * @mbggenerated
     */
    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_test.name
     *
     * @mbggenerated
     */
    public String getName() {
        return name;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_test.name
     *
     * @mbggenerated
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_test.sex
     *
     * @mbggenerated
     */
    public String getSex() {
        return sex;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_test.sex
     *
     * @mbggenerated
     */
    public void setSex(String sex) {
        this.sex = sex == null ? null : sex.trim();
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_test.status
     *
     * @mbggenerated
     */
    public String getStatus() {
        return status;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_test.status
     *
     * @mbggenerated
     */
    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_test.time
     *
     * @mbggenerated
     */
    public Date getTime() {
        return time;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_test.time
     *
     * @mbggenerated
     */
    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_test.created
     *
     * @mbggenerated
     */
    public Date getCreated() {
        return created;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_test.created
     *
     * @mbggenerated
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_test.test_id
     *
     * @mbggenerated
     */
    public String getTest_id() {
        return test_id;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_test.test_id
     *
     * @mbggenerated
     */
    public void setTest_id(String test_id) {
        this.test_id = test_id == null ? null : test_id.trim();
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_test.love_name
     *
     * @mbggenerated
     */
    public String getLove_name() {
        return love_name;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_test.love_name
     *
     * @mbggenerated
     */
    public void setLove_name(String love_name) {
        this.love_name = love_name == null ? null : love_name.trim();
    }

    /**
     * 对应映射sql中的数据
     *
     * @param rs
     * @throws SQLException
     */
    public void mappingColumn(ResultSet rs) throws SQLException {
        this.setId(rs.getInt("id"));
        this.setLove_name(rs.getString("love_name"));
        this.setName(rs.getString("name"));
        this.setUsername(rs.getString("username"));
        this.setCreated(rs.getDate("created"));
        this.setTime(rs.getDate("time"));
        this.setSex(rs.getString("sex"));
        this.setStatus(rs.getString("status"));
        this.setTest_id(rs.getString("test_id"));
    }
}