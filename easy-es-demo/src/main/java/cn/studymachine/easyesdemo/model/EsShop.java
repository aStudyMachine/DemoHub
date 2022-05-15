package cn.studymachine.easyesdemo.model;

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
import java.util.List;

import static cn.studymachine.easyesdemo.model.EsShop.INDEX_NAME;

/**
 * @author wukun
 * @since 2022/5/14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(INDEX_NAME)
@FieldNameConstants
public class EsShop implements Serializable {


    /**
     * The constant serialVersionUID.
     */
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    @TableField(exist = false)
    public static final String INDEX_NAME = "vyc_shop";

    /**
     * es中的唯一id, 这里与{@link EsShop#shopId }一致
     */
    @TableId(type = IdType.AUTO)
    private String id;

    /**
     * 门店id
     */
    private Integer shopId;

    /**
     * 名店名称
     */
    @TableField(fieldType = FieldType.TEXT)
    private String shopName;

    /**
     * 省份
     */
    @TableField(fieldType = FieldType.TEXT)
    private String shopProvince;

    /**
     * 城市
     */
    @TableField(fieldType = FieldType.TEXT)
    private String shopCity;

    /**
     * 区域
     */
    @TableField(fieldType = FieldType.TEXT)
    private String shopArea;

    /**
     * 详细地址
     */
    @TableField(fieldType = FieldType.TEXT)
    private String shopAddressDetail;

    /**
     * 门店评分 0-500
     */
    private Integer score;

    /**
     * 门店坐标. 格式: "lat,lon"
     */
    @TableField(fieldType = FieldType.GEO_POINT)
    private String location;

    /**
     * 门店类型 (v车店 主营业务. saleIds)
     */
    private List<String> shopType;


    // 不支持  "1,2,3" 格式
    // /**
    //  * 门店类型 (v车店 主营业务. saleIds)
    //  */
    // @TableField(fieldType = FieldType.ARRAY)
    // private String shopType;

}
