package ObjectDuibi;

import com.esotericsoftware.reflectasm.MethodAccess;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.*;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 利用MethodAccess 反射方式注入进去
 *
 * @author Liukx
 * @create 2017-03-17 17:23
 * @email liukx@elab-plus.com
 **/
public class RowMapper4<T> implements RowMapper<T> {

    /**
     * Logger available to subclasses
     */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * The class we are mapping to
     */
    private Class<T> mappedClass;

    /**
     * Whether we're strictly validating
     */
    private boolean checkFullyPopulated = false;

    /**
     * Whether we're defaulting primitives when mapping a null value
     */
    private boolean primitivesDefaultedForNullValue = false;

    /**
     * Map of the fields we provide mapping for
     */
    private Map<String, PropertyDescriptor> mappedFields;

    /**
     * Set of bean properties we provide mapping for
     */
    private Set<String> mappedProperties;


    /**
     * Create a new BeanPropertyRowMapper for bean-style configuration.
     *
     * @see #setMappedClass
     * @see #setCheckFullyPopulated
     */
    public RowMapper4() {
    }

    /**
     * Create a new BeanPropertyRowMapper, accepting unpopulated properties
     * in the target bean.
     * <p>Consider using the {@link #newInstance} factory method instead,
     * which allows for specifying the mapped type once only.
     *
     * @param mappedClass the class that each row should be mapped to
     */
    public RowMapper4(Class<T> mappedClass) {
        initialize(mappedClass);
    }

    /**
     * Create a new BeanPropertyRowMapper.
     *
     * @param mappedClass         the class that each row should be mapped to
     * @param checkFullyPopulated whether we're strictly validating that
     *                            all bean properties have been mapped from corresponding database fields
     */
    public RowMapper4(Class<T> mappedClass, boolean checkFullyPopulated) {
        initialize(mappedClass);
        this.checkFullyPopulated = checkFullyPopulated;
    }


    /**
     * Set the class that each row should be mapped to.
     */
    public void setMappedClass(Class<T> mappedClass) {
        if (this.mappedClass == null) {
            initialize(mappedClass);
        } else {
            if (!this.mappedClass.equals(mappedClass)) {
                throw new InvalidDataAccessApiUsageException("The mapped class can not be reassigned to map to " +
                        mappedClass + " since it is already providing mapping for " + this.mappedClass);
            }
        }
    }

    /**
     * Initialize the mapping metadata for the given class.
     *
     * @param mappedClass the mapped class.
     */
    protected void initialize(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
        this.mappedFields = new HashMap<String, PropertyDescriptor>();
        this.mappedProperties = new HashSet<String>();
        PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(mappedClass);
        for (PropertyDescriptor pd : pds) {
            if (pd.getWriteMethod() != null) {
                this.mappedFields.put(pd.getName().toLowerCase(), pd);
                String underscoredName = underscoreName(pd.getName());
                if (!pd.getName().toLowerCase().equals(underscoredName)) {
                    this.mappedFields.put(underscoredName, pd);
                }
                this.mappedProperties.add(pd.getName());
            }
        }
    }

    /**
     * Convert a name in camelCase to an underscored name in lower case.
     * Any upper case letters are converted to lower case with a preceding underscore.
     *
     * @param name the string containing original name
     * @return the converted name
     */
    private String underscoreName(String name) {
        if (!StringUtils.hasLength(name)) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append(name.substring(0, 1).toLowerCase());
        for (int i = 1; i < name.length(); i++) {
            String s = name.substring(i, i + 1);
            String slc = s.toLowerCase();
            if (!s.equals(slc)) {
                result.append("_").append(slc);
            } else {
                result.append(s);
            }
        }
        return result.toString();
    }

    /**
     * Get the class that we are mapping to.
     */
    public final Class<T> getMappedClass() {
        return this.mappedClass;
    }

    /**
     * Set whether we're strictly validating that all bean properties have been
     * mapped from corresponding database fields.
     * <p>Default is {@code false}, accepting unpopulated properties in the
     * target bean.
     */
    public void setCheckFullyPopulated(boolean checkFullyPopulated) {
        this.checkFullyPopulated = checkFullyPopulated;
    }

    /**
     * Return whether we're strictly validating that all bean properties have been
     * mapped from corresponding database fields.
     */
    public boolean isCheckFullyPopulated() {
        return this.checkFullyPopulated;
    }

    /**
     * Set whether we're defaulting Java primitives in the case of mapping a null value
     * from corresponding database fields.
     * <p>Default is {@code false}, throwing an exception when nulls are mapped to Java primitives.
     */
    public void setPrimitivesDefaultedForNullValue(boolean primitivesDefaultedForNullValue) {
        this.primitivesDefaultedForNullValue = primitivesDefaultedForNullValue;
    }

    /**
     * Return whether we're defaulting Java primitives in the case of mapping a null value
     * from corresponding database fields.
     */
    public boolean isPrimitivesDefaultedForNullValue() {
        return primitivesDefaultedForNullValue;
    }


    public static String firstLetterToUpper(String val) {
        char[] array = val.toCharArray();
        array[0] -= 32;
        return String.valueOf(array);
    }

    /**
     * Extract the values for all columns in the current row.
     * <p>Utilizes public setters and result set metadata.
     *
     * @see ResultSetMetaData
     */
    public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
        Assert.state(this.mappedClass != null, "Mapped class was not specified");
        T mappedObject = BeanUtils.instantiate(this.mappedClass);
        BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);
        initBeanWrapper(bw);
        MethodAccess ma = MethodAccess.get(mappedObject.getClass());
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        Set<String> populatedProperties = (isCheckFullyPopulated() ? new HashSet<String>() : null);

        for (int index = 1; index <= columnCount; index++) {
            String column = JdbcUtils.lookupColumnName(rsmd, index);
            PropertyDescriptor pd = this.mappedFields.get(column.replaceAll(" ", "").toLowerCase());
            if (pd != null) {
                try {
                    Object value = getColumnValue(rs, index, pd);
//                    if (value == null) {
//                        continue;
//                    }
                    try {
                        int addNameIndex = ma.getIndex("set" + firstLetterToUpper(pd.getName()));
//                        bw.setPropertyValue(pd.getName(), value);
                        ma.invoke(mappedObject, addNameIndex, value);
                    } catch (TypeMismatchException e) {

                    }
                    if (populatedProperties != null) {
                        populatedProperties.add(pd.getName());
                    }
                } catch (NotWritablePropertyException ex) {

                }
            }
        }

        if (populatedProperties != null && !populatedProperties.equals(this.mappedProperties)) {

        }

        return mappedObject;
    }

    /**
     * Initialize the given BeanWrapper to be used for row mapping.
     * To be called for each row.
     * <p>The default implementation is empty. Can be overridden in subclasses.
     *
     * @param bw the BeanWrapper to initialize
     */
    protected void initBeanWrapper(BeanWrapper bw) {
    }

    /**
     * Retrieve a JDBC object value for the specified column.
     * <p>The default implementation calls
     * {@link JdbcUtils#getResultSetValue(ResultSet, int, Class)}.
     * Subclasses may override this to check specific value types upfront,
     * or to post-process values return from {@code getResultSetValue}.
     *
     * @param rs    is the ResultSet holding the data
     * @param index is the column index
     * @param pd    the bean property that each result object is expected to match
     *              (or {@code null} if none specified)
     * @return the Object value
     * @throws SQLException in case of extraction failure
     * @see JdbcUtils#getResultSetValue(ResultSet, int, Class)
     */
    protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) throws SQLException {
        return JdbcUtils.getResultSetValue(rs, index, pd.getPropertyType());
    }


    /**
     * Static factory method to create a new BeanPropertyRowMapper
     * (with the mapped class specified only once).
     *
     * @param mappedClass the class that each row should be mapped to
     */
    public static <T> BeanPropertyRowMapper<T> newInstance(Class<T> mappedClass) {
        BeanPropertyRowMapper<T> newInstance = new BeanPropertyRowMapper<T>();
        newInstance.setMappedClass(mappedClass);
        return newInstance;
    }

}
