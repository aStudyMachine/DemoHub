package cn.studymachine;

import cn.hutool.core.map.MapUtil;
import com.rabbitmq.client.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@SpringBootTest(classes = RabbitMQDemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(org.springframework.test.context.junit4.SpringRunner.class)
public class TestOne {

    @Autowired
    private ConnectionFactory connectionFactory;

    public static final String QUEUE_NAME = "test.direct.queue"; // 队列名称
    public static final String EXCHANGE_NAME = "test.direct.exchange"; // 交换机名称
    public static final String ROUTING_KEY = "test.direct.routing.key"; // 路由键


    /**
     * 声明队列、direct交换机 &&  绑定 队列与交换机
     *
     * @throws IOException      the io exception
     * @throws TimeoutException the timeout exception
     */
    @Test
    public void declareQueueAndExchange() throws IOException, TimeoutException {
        try (Connection connection = connectionFactory.newConnection(); Channel channel = connection.createChannel()) {

            // 1. 声明队列
            boolean durable = true;        // 持久化队列
            boolean exclusive = false;     // 非排他队列
            boolean autoDelete = false;    // 非自动删除
            channel.queueDeclare(QUEUE_NAME, durable, exclusive, autoDelete, null);

            // 2. 声明交换机
            boolean exchangeDurable = true; // 持久化交换机
            boolean exchangeAutoDelete = false; // 非自动删除
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, exchangeDurable, exchangeAutoDelete, null);

            // 3. 绑定队列与交换机
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

            System.out.println("队列、交换机声明和绑定完成");
        }
    }


    /**
     * 发送消息到交换机
     *
     * @throws IOException      the io exception
     * @throws TimeoutException the timeout exception
     */
    @Test
    public void sendMessage() throws IOException, TimeoutException {
        try (Connection connection = connectionFactory.newConnection(); Channel channel = connection.createChannel()) {

            // 1. 准备消息内容
            String message = "这是一条测试消息 " + System.currentTimeMillis();

            // 2. 发送消息到交换机
            // 参数1: 交换机名称
            // 参数2: 路由键，与队列绑定时的路由键保持一致
            // 参数3: 消息的属性
            // 参数4: 消息体(字节数组)
            channel.basicPublish(EXCHANGE_NAME,  // 交换机名称
                    ROUTING_KEY,     // 路由键
                    new AMQP.BasicProperties(),           // 消息属性，如持久化、过期时间等，这里使用默认值
                    message.getBytes() // 消息体
            );

            System.out.println("✅ 消息发送成功: " + message);
        }
    }

    /**
     * 接收消息
     *
     * @throws IOException          the io exception
     * @throws TimeoutException     the timeout exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    public void receiveMessage() throws IOException, TimeoutException, InterruptedException {
        // 1. 创建连接和通道
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        // 2. 设置消费者预取消息的数量为1，即一次只处理一条消息
        channel.basicQos(1);

        System.out.println("✅ 等待接收消息...");

        // 3. 创建消费者并订阅队列
        // 参数1: 队列名称
        // 参数2: 是否自动确认，设为false表示需要手动确认消息已处理
        // 参数3: 消费者回调函数，处理收到的消息
        channel.basicConsume(QUEUE_NAME, false, (consumerTag, delivery) -> {
            // 获取消息内容
            String receivedMessage = new String(delivery.getBody());

            // 获取投递标签，用于手动确认
            long deliveryTag = delivery.getEnvelope().getDeliveryTag();

            // 获取交换机和路由键信息
            String exchange = delivery.getEnvelope().getExchange();
            String routingKey = delivery.getEnvelope().getRoutingKey();

            System.out.println("📩 收到消息: " + receivedMessage);
            System.out.println("📝 消息详情 - 交换机: " + exchange + ", 路由键: " + routingKey);

            try {
                // 模拟消息处理
                System.out.println("⏳ 正在处理消息...");
                Thread.sleep(1000);
                System.out.println("✅ 消息处理完成");

                // 手动确认消息已被成功处理
                // 参数1: 投递标签
                // 参数2: 是否批量确认，false表示只确认当前消息
                channel.basicAck(deliveryTag, false);
            } catch (Exception e) {
                System.out.println("❌ 消息处理失败: " + e.getMessage());

                // 消息处理失败，拒绝消息
                // 参数1: 投递标签
                // 参数2: 是否批量拒绝，false表示只拒绝当前消息
                // 参数3: 是否重新入队，true表示将消息重新放回队列，false表示丢弃消息
                channel.basicReject(deliveryTag, true);
            }
        }, consumerTag -> {
            // 消费者取消回调函数
            System.out.println("⚠️ 消费者被取消: " + consumerTag);
        });

        // 4. 让测试方法保持运行一段时间，等待消息到达
        // 注意：在实际应用中，消费者通常在应用启动时就开始运行，而不是在测试方法中
        Thread.sleep(10000);

        // 5. 关闭资源
        channel.close();
        connection.close();
    }

    @Test
    public void sendMessageWithProperties() throws IOException, TimeoutException {
        try (Connection connection = connectionFactory.newConnection(); Channel channel = connection.createChannel()) {

            // 1. 准备消息内容
            String message = "这是一条带有属性的测试消息 " + System.currentTimeMillis();

            // 2. 设置消息属性
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .contentType("text/plain") // 内容类型
                    .contentEncoding("UTF-8")  // 内容编码
                    .deliveryMode(2)           // 2表示持久化消息 , 1表示非持久化消息 , 默认是1
                    .priority(1)               // 优先级，0-9，数字越大优先级越高
                    .correlationId("msg-" + System.currentTimeMillis()) // 关联ID，用于RPC场景
                    .expiration("100000")      // 过期时间，单位毫秒, 超过时间后消息会被删除
                    .headers(MapUtil.ofEntries(
                            MapUtil.entry("source", "test"),
                            MapUtil.entry("timestamp", System.currentTimeMillis()))
                    )// 自定义消息头
                    .build();


            // 3. 发送消息到交换机
            channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, properties, message.getBytes());

            System.out.println("✅ 带属性的消息发送成功: " + message);
        }
    }

    @Test
    public void sendBatchMessages() throws IOException, TimeoutException, InterruptedException {
        try (Connection connection = connectionFactory.newConnection(); Channel channel = connection.createChannel()) {

            // 开启发布者确认模式
            channel.confirmSelect();

            // 批量发送10条消息
            int messageCount = 10;

            System.out.println("⏳ 开始批量发送消息...");
            for (int i = 1; i <= messageCount; i++) {
                String message = "批量消息 #" + i + " - " + System.currentTimeMillis();

                channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, message.getBytes());

                System.out.println("📤 已发送: " + message);
            }

            // 等待服务器确认所有消息
            boolean allConfirmed = channel.waitForConfirms(5000); // 等待5秒
            if (allConfirmed) {
                System.out.println("✅ 所有消息都已被服务器确认");
            } else {
                System.out.println("❌ 部分消息未被确认");
            }
        }
    }


}
