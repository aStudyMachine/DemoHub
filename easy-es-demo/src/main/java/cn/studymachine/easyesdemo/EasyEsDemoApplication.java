package cn.studymachine.easyesdemo;

import com.xpc.easyes.autoconfig.annotation.EsMapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author wukun
 * @since 2022/5/14
 */
@SpringBootApplication
@EsMapperScan("cn.studymachine.easyesdemo.mapper")
public class EasyEsDemoApplication {

    /*---------------------------------------------- Fields ~ ----------------------------------------------*/



    /*---------------------------------------------- Methods ~ ----------------------------------------------*/

    public static void main(String[] args) {
        SpringApplication.run(EasyEsDemoApplication.class);
    }

}
