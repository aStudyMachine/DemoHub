package cn.studymachine;


import com.rabbitmq.client.AMQP;
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


    /**
     * 消息过期后进入死信队列
     */
    @Test
    public void testDeadLetterByMessageExpiration() throws IOException, TimeoutException, InterruptedException {
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            // 1. 准备消息内容
            String message = "这是一条会过期的消息 " + System.currentTimeMillis();

            // 2. 设置消息属性，包括TTL（过期时间）
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .expiration("5000")  // 设置过期时间为5秒
                    .build();

            // 3. 发送消息到交换机
            channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, properties, message.getBytes());
            System.out.println("✅ 带TTL的消息发送成功: " + message);

            // 4. 等待消息过期（比TTL长一些）
            Thread.sleep(6000);

            // 5. 消费死信队列
            channel.basicConsume(DEAD_LETTER_QUEUE_NAME, true, (consumerTag, delivery) -> {
                String receivedMessage = new String(delivery.getBody());
                System.out.println("💀 从死信队列收到过期消息: " + receivedMessage);
            }, consumerTag -> {
            });

            // 让程序持续运行一段时间
            Thread.sleep(5000);

        }
    }



    // --------------------- 测试超过最大队列长度进入死信队列  ---------------------

    /**
     * 设置队列最大长度和死信交换机
     */
    @Test
    public void setupQueueWithMaxLength() throws IOException, TimeoutException {
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            // 先删除可能已经存在的队列
            channel.queueDelete(QUEUE_NAME);

            // 声明死信交换机和队列（同上）
            channel.exchangeDeclare(DEAD_LETTER_EXCHANGE_NAME, "direct", true);
            channel.queueDeclare(DEAD_LETTER_QUEUE_NAME, true, false, false, null);
            channel.queueBind(DEAD_LETTER_QUEUE_NAME, DEAD_LETTER_EXCHANGE_NAME, DEAD_LETTER_ROUTING_KEY);

            // 声明普通交换机
            channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);

            // 声明普通队列，设置最大长度和死信参数
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE_NAME);
            arguments.put("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY);
            arguments.put("x-max-length", 3);  // 设置队列最大长度为3

            channel.queueDeclare(QUEUE_NAME, true, false, false, arguments);
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

            System.out.println("✅ 创建了最大长度为3的队列及死信结构");
        }
    }

    @Test
    public void testDeadLetterByMaxLength() throws IOException, TimeoutException, InterruptedException {
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            // 发送5条消息，超过队列最大长度
            for (int i = 1; i <= 5; i++) {
                String message = "消息 #" + i + " - " + System.currentTimeMillis();
                channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, message.getBytes());
                System.out.println("📤 发送消息: " + message);
            }

            System.out.println("✅ 发送了5条消息到最大长度为3的队列");

            // 等待一会儿，让消息传递到死信队列
            Thread.sleep(1000);

            // 从普通队列消费消息
            System.out.println("🔍 查看普通队列中的消息:");
            channel.basicConsume(QUEUE_NAME, true, (consumerTag, delivery) -> {
                String receivedMessage = new String(delivery.getBody());
                System.out.println("📩 从普通队列收到: " + receivedMessage);
            }, consumerTag -> {
            });

            // 从死信队列消费消息
            System.out.println("🔍 查看死信队列中的消息:");
            channel.basicConsume(DEAD_LETTER_QUEUE_NAME, true, (consumerTag, delivery) -> {
                String receivedMessage = new String(delivery.getBody());
                System.out.println("💀 从死信队列收到: " + receivedMessage);
            }, consumerTag -> {
            });

            // 让程序持续运行一段时间
            Thread.sleep(5000);
        }
    }

}
