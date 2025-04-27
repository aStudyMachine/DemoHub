package cn.studymachine.controller;

import cn.studymachine.config.SimpleMessageListenerContainerExampleConfig;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RequestMapping("/test01")
    public void test01() {
        rabbitTemplate.convertAndSend(
                SimpleMessageListenerContainerExampleConfig.EXCHANGE_NAME,
                SimpleMessageListenerContainerExampleConfig.ROUTING_KEY,
                "test SimpleMessageListenerContainerExample" + System.currentTimeMillis(),
                (message) -> message,
                new CorrelationData(String.valueOf(System.currentTimeMillis()
                )));

    }


}
