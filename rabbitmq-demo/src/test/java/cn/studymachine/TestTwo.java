package cn.studymachine;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * 死信队列 测试
 */
@SpringBootTest(classes = RabbitMQDemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(org.springframework.test.context.junit4.SpringRunner.class)
public class TestTwo {

    @Autowired
    private ConnectionFactory connectionFactory;

    public static final String QUEUE_NAME = "queue.test.dead.letter"; // 队列名称
    public static final String EXCHANGE_NAME = "exchange.test.dead.letter"; // 交换机名称
    public static final String ROUTING_KEY = "key.test.dead.letter"; // 路由键

    public static final String DEAD_LETTER_QUEUE_NAME = "dlq.test.dead.letter"; // 死信队列名称
    public static final String DEAD_LETTER_EXCHANGE_NAME = "dlx.test.dead.letter"; // 死信交换机名称
    public static final String DEAD_LETTER_ROUTING_KEY = "dlx.test.dead.letter"; // 死信路由键

    @Test
    public void setupDeadLetterQueueInfrastructure() throws IOException, TimeoutException {
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            // 1. 声明死信交换机
            channel.exchangeDeclare(DEAD_LETTER_EXCHANGE_NAME, "direct", true);

            // 2. 声明死信队列
            channel.queueDeclare(DEAD_LETTER_QUEUE_NAME, true, false, false, null);

            // 3. 绑定死信队列到死信交换机
            channel.queueBind(DEAD_LETTER_QUEUE_NAME, DEAD_LETTER_EXCHANGE_NAME, DEAD_LETTER_ROUTING_KEY);

            // 4. 声明普通交换机
            channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);

            // 5. 声明普通队列，并设置死信参数
            Map<String, Object> arguments = new HashMap<>();
            // 设置死信交换机
            arguments.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE_NAME);
            // 设置死信路由键
            arguments.put("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY);

            // 6. 声明普通队列
            channel.queueDeclare(QUEUE_NAME, true, false, false, arguments);

            // 7. 绑定普通队列到普通交换机
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

            System.out.println("✅ 死信队列相关结构创建成功");
        }
    }


    /**
     * 消息被拒绝后进入死信队列
     *
     * @throws IOException          the io exception
     * @throws TimeoutException     the timeout exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    public void testDeadLetterByRejection() throws IOException, TimeoutException, InterruptedException {
        // 1. 创建连接和通道
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        // 2. 发送一条消息到正常队列
        String message = "这是一条被拒绝的消息 " + System.currentTimeMillis();
        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, message.getBytes());
        System.out.println("✅ 消息发送成功: " + message);

        // 3. 消费消息并拒绝
        channel.basicConsume(QUEUE_NAME, false, (consumerTag, delivery) -> {
            String receivedMessage = new String(delivery.getBody());
            System.out.println("📩 收到消息: " + receivedMessage);

            // 拒绝消息，不重新入队（设置requeue=false），将进入死信队列
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
            System.out.println("❌ 消息被拒绝，将进入死信队列");

        }, consumerTag -> {
        });

        // 4. 等待一段时间
        Thread.sleep(5000);

        // 5. 再创建一个消费者监听死信队列
        channel.basicConsume(DEAD_LETTER_QUEUE_NAME, true, (consumerTag, delivery) -> {
            String receivedMessage = new String(delivery.getBody());
            System.out.println("💀 从死信队列收到消息: " + receivedMessage);
        }, consumerTag -> {
        });

        // 让程序持续运行一段时间
        Thread.sleep(5000);

        // 关闭资源
        channel.close();
        connection.close();
    }



}
