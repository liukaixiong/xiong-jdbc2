package model;

import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import java.util.Date;
import java.util.List;

public class TGirl {
    //
    // 表字段 : t_girl.id
    private Integer id;

    //
    // 表字段 : t_girl.girl_name
    private String girlName;

    //
    // 表字段 : t_girl.age
    private Integer age;

    //
    // 表字段 : t_girl.status
    private Integer status;

    //
    // 表字段 : t_girl.created
    private Date created;

    @JoinTable(schema = "dao.DaoInterface.selectByExample", joinColumns = {
            @JoinColumn(name = "id", referencedColumnName = "id"),
            @JoinColumn(name = "status", referencedColumnName = "status")
    })
    private TTest test;

    public TTest getTest() {
        return test;
    }

    public void setTest(TTest test) {
        this.test = test;
    }

    @JoinTable(schema = "dao.DaoInterface.selectByExample", joinColumns = {
            @JoinColumn(name = "id", referencedColumnName = "test_id"),
            @JoinColumn(name = "status", referencedColumnName = "status")
    })
    private List<TTest> testList;


    public List<TTest> getTestList() {
        return testList;
    }

    public void setTestList(List<TTest> testList) {
        this.testList = testList;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_girl.id
     *
     * @mbg.generated
     */
    public Integer getId() {
        return id;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_girl.id
     *
     * @mbg.generated
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_girl.girl_name
     *
     * @mbg.generated
     */
    public String getGirlName() {
        return girlName;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_girl.girl_name
     *
     * @mbg.generated
     */
    public void setGirlName(String girlName) {
        this.girlName = girlName == null ? null : girlName.trim();
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_girl.age
     *
     * @mbg.generated
     */
    public Integer getAge() {
        return age;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_girl.age
     *
     * @mbg.generated
     */
    public void setAge(Integer age) {
        this.age = age;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_girl.status
     *
     * @mbg.generated
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_girl.status
     *
     * @mbg.generated
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_girl.created
     *
     * @mbg.generated
     */
    public Date getCreated() {
        return created;
    }

    /**
     * 描述 ：
     * 对应数据库： ->  t_girl.created
     *
     * @mbg.generated
     */
    public void setCreated(Date created) {
        this.created = created;
    }
}