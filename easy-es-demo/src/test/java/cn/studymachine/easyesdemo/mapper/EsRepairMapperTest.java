package cn.studymachine.easyesdemo.mapper;

import cn.studymachine.easyesdemo.BootApplicationTest;
import cn.studymachine.easyesdemo.model.EsRepair;
import com.alibaba.fastjson.JSON;
import com.xpc.easyes.core.common.PageInfo;
import com.xpc.easyes.core.conditions.LambdaEsQueryWrapper;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.search.sort.SortBuilders;
import org.junit.Test;

import javax.annotation.Resource;

public class EsRepairMapperTest extends BootApplicationTest {

    @Resource
    private EsRepairMapper esRepairMapper;



    @Test
    public void searchTest() {
        LambdaEsQueryWrapper<EsRepair> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.sort(SortBuilders.geoDistanceSort(EsRepair.Fields.location, new GeoPoint("23.12957839996682,113.33456028912352")));
        PageInfo<EsRepair> esRepairPageInfo = esRepairMapper.pageQuery(wrapper, 1, 20);
        System.out.println("===============================");
        System.out.println(JSON.toJSONString(esRepairPageInfo.getList()));
    }

}