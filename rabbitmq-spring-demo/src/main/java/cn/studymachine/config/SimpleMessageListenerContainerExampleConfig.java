package cn.studymachine.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SimpleMessageListenerContainerExampleConfig {

    public static final String QUEUE_NAME = "queue.simple.message.listener.container";
    public static final String EXCHANGE_NAME = "exchange.simple.message.listener.container";
    public static final String ROUTING_KEY = "routing.key.simple.message.listener.container";


    // 定义一个队列
    @Bean(name = "queueSimpleMessageListenerContainer")
    public Queue queue() {
        return new Queue("queue.simple.message.listener.container");
    }

    // 定义一个交换机
    @Bean(name = "exchangeSimpleMessageListenerContainer")
    public DirectExchange directExchange() {
        return new DirectExchange("exchange.simple.message.listener.container");
    }

    // 定义一个绑定关系
    @Bean
    public Binding binding(Queue queueSimpleMessageListenerContainer, DirectExchange exchangeSimpleMessageListenerContainer) {
        return BindingBuilder.bind(queueSimpleMessageListenerContainer).to(exchangeSimpleMessageListenerContainer).with("routing.key.simple.message.listener.container");
    }


    /**
     * 创建一个简单的消息监听容器
     */
    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory, Queue queueSimpleMessageListenerContainer) {

        // 创建SimpleMessageListenerContainer实例
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();

        // 设置连接工厂
        container.setConnectionFactory(connectionFactory);

        // 设置要监听的队列名称
        container.setQueueNames(queueSimpleMessageListenerContainer.getName());

        // 设置并发消费者的最小数量
        container.setConcurrentConsumers(1);

        // 设置并发消费者的最大数量
        container.setMaxConcurrentConsumers(5);

        // 设置预取计数 - 每个消费者一次从broker获取的消息数量
        container.setPrefetchCount(10);

        // 设置是否自动启动容器，默认true
        container.setAutoStartup(true);

        // 设置消费者的标签策略
        container.setConsumerTagStrategy(queue -> "consumer-" + queue + "-" + System.currentTimeMillis());

        // 设置消息确认模式:
        // AcknowledgeMode.AUTO - 自动确认(默认)
        // AcknowledgeMode.MANUAL - 手动确认(需要在监听器中调用channel.basicAck)
        // AcknowledgeMode.NONE - 不确认(等同于自动确认)
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);

        // 设置消息监听器 - 使用ChannelAwareMessageListener可以获取到Channel对象
        container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
            try {
                // 获取消息内容
                String messageBody = new String(message.getBody());

                // 获取消息属性
                MessageProperties properties = message.getMessageProperties();

                // 打印接收到的消息信息
                log.info("收到消息: {} 消息ID: {}  路由键: {}  交换机: {}  投递标签: {}",
                        messageBody, properties.getMessageId(), properties.getReceivedRoutingKey(), properties.getReceivedExchange(), properties.getDeliveryTag());


                // 在这里处理业务逻辑...

                // 如果确认模式是MANUAL，需要手动确认消息
                // channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

            } catch (Exception e) {
                // 处理异常
                System.err.println("处理消息时发生异常: " + e.getMessage());

                // 如果确认模式是MANUAL，可以拒绝消息并选择是否重新入队
                // channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            }
        });

        // 设置当监听器抛出异常时的恢复间隔(毫秒)
        container.setRecoveryInterval(5000);

        // 设置当RabbitMQ管理控制台关闭连接时是否重新声明队列
        container.setMissingQueuesFatal(false);
        // 设置是否在启动时重新声明队列
        container.setDeclarationRetries(3);
        // 设置每次声明重试之间的间隔(毫秒)
        container.setRetryDeclarationInterval(1000);

        return container;
    }

}
