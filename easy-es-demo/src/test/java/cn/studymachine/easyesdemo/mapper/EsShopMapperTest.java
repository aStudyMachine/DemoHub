package cn.studymachine.easyesdemo.mapper;
import cn.hutool.core.util.RandomUtil;
import cn.studymachine.easyesdemo.BootApplicationTest;
import cn.studymachine.easyesdemo.model.EsRepair;
import cn.studymachine.easyesdemo.model.EsShop;
import com.alibaba.fastjson.JSON;
import com.xpc.easyes.core.common.PageInfo;
import com.xpc.easyes.core.conditions.LambdaEsQueryWrapper;
import org.assertj.core.util.Lists;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.search.sort.SortBuilders;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

public class EsShopMapperTest extends BootApplicationTest {

    @Resource
    private EsShopMapper esShopMapper;


    @Resource
    private EsRepairMapper esRepairMapper;


    @Test
    public void saveTest() {

        LambdaEsQueryWrapper<EsRepair> repairWrapper = new LambdaEsQueryWrapper<>();
        repairWrapper.sort(SortBuilders.geoDistanceSort(EsRepair.Fields.location, new GeoPoint("23.12957839996682,113.33456028912352")));
        PageInfo<EsRepair> esRepairPageInfo = esRepairMapper.pageQuery(repairWrapper, 0, 30);


        List<EsShop> insertData = new ArrayList<>();

        for (EsRepair esRepair : esRepairPageInfo.getList()) {
            EsShop esShop = new EsShop();
            esShop.setShopId(esRepair.getRepairId());
            esShop.setShopName(esShop.getShopName());
            esShop.setShopProvince(esRepair.getQdProvince());
            esShop.setShopCity(esRepair.getQdCity());
            esShop.setShopArea(esRepair.getRepairArea());
            esShop.setShopAddressDetail(esRepair.getRepairAddressDetail());
            esShop.setScore(RandomUtil.randomInt(480,500));
            esShop.setLocation(esRepair.getLocation().toGeoPointStr());
            List<String> shopType = Lists.newArrayList(RandomUtil.randomNumbers(1),
                    RandomUtil.randomNumbers(1),
                    RandomUtil.randomNumbers(1));
            esShop.setShopType(shopType);

            insertData.add(esShop);
        }


        esShopMapper.insertBatch(insertData);
    }


    @Test
    public void pageTest() {
        LambdaEsQueryWrapper<EsShop> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(EsShop::getShopType, 1);
        wrapper.sort(SortBuilders.geoDistanceSort(EsRepair.Fields.location, new GeoPoint("23.12957839996682,113.33456028912352")));
        PageInfo<EsShop> esShopPageInfo = esShopMapper.pageQuery(wrapper, 1, 10);

        System.out.println(JSON.toJSONString(esShopPageInfo.getList()));
    }


}