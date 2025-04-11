package cn.studymachine.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
     * rabbit template 设置 发布确认回调 && 消息返回回调
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 发布确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息发送成功: {}", correlationData);
            } else {
                log.error("消息发送失败: {}, 原因: {}", correlationData, cause);
            }
        });

        // // 旧版消息返回回调
        // rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
        //      log.error("消息未被路由到队列: {}, 回复码: {}, 回复文本: {}, 交换机: {}, 路由键: {}",
        //             message, replyCode, replyText, exchange, routingKey);
        // });

        // 设置ReturnCallback（旧版用setReturnCallback，新版使用setReturnsCallback）
        rabbitTemplate.setReturnsCallback(returned -> {
            System.out.println("消息被退回：" +
                    "\n消息主体：" + new String(returned.getMessage().getBody()) +
                    "\n应答码：" + returned.getReplyCode() +
                    "\n描述：" + returned.getReplyText() +
                    "\n交换机：" + returned.getExchange() +
                    "\n路由键：" + returned.getRoutingKey());
        });

        return rabbitTemplate;
    }


}
