package cn.studymachine.config;

import com.rabbitmq.client.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author wukun
 * @since 2025/2/4
 */
@Configuration
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class RabbitmqConfig {

    /*---------------------------------------------- Fields ~ ----------------------------------------------*/



    /*---------------------------------------------- Methods ~ ----------------------------------------------*/


    /**
     * RabbitMQ 连接工厂 【原生 rabbit mq sdk】
     * <p>
     *
     * @return connection factory
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");     // RabbitMQ 服务器地址
        factory.setPort(5672);            // 默认端口
        factory.setVirtualHost("/");      // 虚拟主机（VHost）
        factory.setUsername("guest");     // 默认账号
        factory.setPassword("guest");     // 默认密码
        return factory;
    }



}
