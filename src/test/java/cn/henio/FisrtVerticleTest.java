package cn.henio;import io.netty.handler.codec.mqtt.MqttQoS;import io.vertx.core.Vertx;import io.vertx.core.json.JsonObject;import io.vertx.ext.unit.Async;import io.vertx.ext.unit.TestContext;import io.vertx.ext.unit.junit.VertxUnitRunner;import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;import org.eclipse.paho.client.mqttv3.IMqttToken;import org.eclipse.paho.client.mqttv3.MqttCallback;import org.eclipse.paho.client.mqttv3.MqttClient;import org.eclipse.paho.client.mqttv3.MqttConnectOptions;import org.eclipse.paho.client.mqttv3.MqttException;import org.eclipse.paho.client.mqttv3.MqttMessage;import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;import org.junit.After;import org.junit.Before;import org.junit.Test;import org.junit.runner.RunWith;/** * created by mouzhanpeng at 2018/2/6 13:31 * * @since jdk 1.9.0 */@RunWith(VertxUnitRunner.class)public class FisrtVerticleTest {    private Vertx vertx;    private MqttClient client;    @Before    public void setUp(TestContext context) {//        vertx = Vertx.vertx();//        DeploymentOptions options = new DeploymentOptions();//        vertx.deployVerticle(MqttVerticle.class.getName(), options, context.asyncAssertSuccess(System.out::println));        try {            client = new MqttClient("tcp://localhost:1883", "543089605", new MemoryPersistence());            client.setCallback(new MqttCallback() {                @Override                public void messageArrived(String topic, MqttMessage message) throws Exception {                    System.out.println("topic="+topic+";receive message=" + message.toString()+";id=" + message.getId());                }                @Override                public void deliveryComplete(IMqttDeliveryToken token) {                }                @Override                public void connectionLost(Throwable cause) {                    System.out.println("connectionLost");                    System.out.println(cause);                    cause.printStackTrace();                }            });        } catch (MqttException me) {            System.out.println("reason " + me.getReasonCode());            System.out.println("msg " + me.getMessage());            System.out.println("loc " + me.getLocalizedMessage());            System.out.println("cause " + me.getCause());            System.out.println("excep " + me);            me.printStackTrace();        } catch (Exception e) {            e.printStackTrace();        }    }    @After    public void tearDown(TestContext context) {        //vertx.close();    }    @Test    public void testApplication(TestContext context) throws MqttException {        final Async async = context.async();        /*vertx.createHttpClient().getNow(8888, "localhost", "/view/test", response -> {            response.handler(body -> {                System.err.println(body.toString());                context.assertTrue(true);                async.complete();            });        })*/;        //连接        connect();        //订阅        subscribe();        try {            Thread.sleep(5000);        } catch (InterruptedException e) {            e.printStackTrace();        }        //发消息        publish();        //取消订阅        unsubscribe();        //断开连接        //disconnect();        context.assertTrue(true);        try {            Thread.sleep(60000);        } catch (InterruptedException e) {            e.printStackTrace();        }        async.complete();    }    private void connect()throws MqttException{        MqttConnectOptions connOpts = new MqttConnectOptions();        connOpts.setUserName("mouzhanpeng");        connOpts.setPassword("12345".toCharArray());        connOpts.setKeepAliveInterval(5);        connOpts.setCleanSession(false);        //connOpts.setWill();  //遗言，非正常断开，服务端发布        IMqttToken connectToken = client.connectWithResult(connOpts);        connectToken.waitForCompletion(5000);        System.err.println(JsonObject.mapFrom(connectToken.getResponse()));    }    private void subscribe() throws MqttException {        client.subscribe(new String[]{"TOP1","TOP2","TOP3"}, new int[]{MqttQoS.AT_MOST_ONCE.value(), MqttQoS.AT_LEAST_ONCE.value(), MqttQoS.AT_LEAST_ONCE.value()});    }    private void unsubscribe() throws MqttException {        client.unsubscribe("TOP2");    }    private void publish() throws MqttException {        client.publish("TOP1", "大鸡腿".getBytes(), MqttQoS.EXACTLY_ONCE.value(), false);    }    private void disconnect() throws MqttException {        client.disconnect();    }}