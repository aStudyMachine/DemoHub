package cn.studymachine.easyesdemo.model;

import cn.hutool.core.date.DatePattern;
import cn.studymachine.easyesdemo.model.dto.LocationDTO;
import com.xpc.easyes.core.anno.TableField;
import com.xpc.easyes.core.anno.TableId;
import com.xpc.easyes.core.anno.TableName;
import com.xpc.easyes.core.enums.FieldType;
import com.xpc.easyes.core.enums.IdType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.util.Date;

import static cn.studymachine.easyesdemo.model.EsRepair.INDEX_NAME;


/**
 * The type Es repair.
 *
 * @author wukun
 * @since 2022 /5/14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(INDEX_NAME)
@FieldNameConstants
public class EsRepair implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableField(exist = false)
    public static final String INDEX_NAME = "repair_data";

    @TableId(type = IdType.CUSTOMIZE)
    private String id;

    private Integer repairId;

    @TableField(fieldType = FieldType.TEXT)
    private String repairProvince;

    private Integer businessStatus;

    @TableField(fieldType = FieldType.TEXT)
    private String repairName;

    @TableField(fieldType = FieldType.DATE, dateFormat = DatePattern.ISO8601_PATTERN)
    private Date insertTime;

    @TableField(fieldType = FieldType.TEXT)
    private String qdUserName;

    @TableField(fieldType = FieldType.TEXT)
    private String repairArea;

    @TableField(fieldType = FieldType.TEXT)
    private String repairAddressDetail;

    @TableField(fieldType = FieldType.TEXT)
    private String repairCity;

    @TableField(fieldType = FieldType.TEXT)
    private String qdProvince;

    private String qdUserId;

    @TableField(fieldType = FieldType.TEXT)
    private String qdCity;

    @TableField(fieldType = FieldType.GEO_POINT)
    private LocationDTO location;

    private Integer state;


}
