package cn.henio;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import io.vertx.core.json.Json;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/5/31 9:53].
 */
public class Refund {

  public static void main(String[] args) throws AlipayApiException {
    final String appId = "2016072001643858";
    final String privateKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCCWjovbo5WviWzxmaZg5D2ETThNyoA138mk9+vwS7ce4vhgY0sgxsYr+knRtUlZj2tDKBGNgC5SSc3RAMpVBh9cReLZeG/3ewWH6jVoOnrJqe4rjEbKxkb5KGGZJ+lSwdOzyfHsXt0O5BisQ6+PtwhP5Qzid2DB/8ekOJsdfc/r+MgL3YG7VBESoh/VVq1G6Ji4F1a1521vlIVjG9VNmlBwTM7p1AVlQBMNrNES4EazQmPC4o5Mj/Vv8aFCW5NAT7fycmApZdhfqX/Q6AplUTaYbJ9no1wfECt3hq12/3o2lXPD88YfbB3R2bWIuAyslnmG96ZEGMU1oKIn97P3UnNAgMBAAECggEARAmSOzWqZ85B4Y7Z+KC6ZiVfA3qGmv/f5yPh6lub+QsnXOIA7M3Vs9IFkTGgiS/PW2autVb0k8GCDY+bUtQJIFiKIIFiDoJn+rg1qKOf1NRNUNDi4rphFbUFNh+JUH5T5yerLMkPlgCNAWZHOreWsGf+E9SO4RioaazX8iK0lfGamFc787x+mleZ+3l2K/gKCqLj69k8JTB5NvB8eSTw3p9R3Xyl4UGY6kCA4z8Uhu8IclPaqaDJ0URjeT+7fyKc43r+OniHF2Fhu4MUhgi80wgJLxPmrcuenxs5gJhp1lMZBfVOxOqOkgNpIzKNuxlS15ROzT43vXuMYubevHtlJQKBgQC4Boq7JprmLeQaOQdiEpbnLRF7f0AcGWl7GL7hfGR/y++G4cxW4mj06M/24bSLkH4gydEYxzPRNcOKwKtmq5iX3DK/ZW6G4hd6jdYIhxTRMOHqup3iS4feDEvbniZbHdy+N6ZE1Fn1i2D0M04iF0t8P8m1roTqf5OD3gZApjwuTwKBgQC1VbH1kJfY1a+Nkk6lvZIt//hHWw44/eD09Q1KeujXpPsXuw8pvag3GFe45FwfcTXwty+0MpZbwBEnG1um33EMkbOhROMRqnQ8U9LJ5Tu4oKOp7DsBx5dlNSdnCQberj7g7sihxXFsgF9VUTjTG/wy77TYsz8bIILx/EMMIK17IwKBgDYHLanenrGfnY2ZWrKPdKBwmeZhKMhXKaHLVRdMdxESJcO6/Uww8tGcnJoGEAS9qlokBnC441yz5TISeQAOtyE2s+t/cyDEtlHz3HfqeNEmAd3xZjMvK7ekco0K9IXh0ZJDMlyY38R4ZEGt4m2d2zqiW60sjLdwNzYK9Fj8tUR/AoGAZH8u1nqhPUIIPjzEq6RpfGbtUZaMRlelsfyRtiJyIvlDU36PQ8HhlU8/Zq/gwmg6zcKohIS4wfarSvIllFEnVSD23bzQ33yJWNnX657Iv2T/72+FsFHOfP+9a6QcE5OcM4sC1G0ZlFl8/Eq8ZHuRBSXGI4cLxVv6VmRJQFJn8uMCgYBafHut4z19nc0H5RS6PsrIwSY1iRBMuDWUMtDZXpmBfaIoWMUEccUOYqA4XWO8gqH5Kst7XVRRhG6ud9b+wVZfqE9N/gJv/kf8VwGS45kQqs7NKvtp/MjKhQgdbfIN3OKoN1PPzecHh4acbu+HB7iZ0BOMG5OAryqri1JkoDTdTQ==";
    final String aliPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoXc/D8WEETYYYW33wLHgrmzpB6pKmhcOa6IRs/Oa5ARK/RxWbUHTrWDbsyZ8VboiZDJqnuGWuHOo7sqzMbahtCNkfwnvGHtBQYAiFunQY4SmtwIGSiYcpidu8O2r8wLjHMOw7FhSBNan9ymgtWGH6YKn4xWGSe7t3uary6DcR2EvIg8iU8gyItqVRWoGOGpDKIff1FFEYA83rj4rx82yYDwyxF+uzpP32TqsRkjBfqibwD64OE9iBfq6xCqEMtI5GF+OYKxKF2SDaffDdXlMQ2Pl0sa+GcaT0tkbLRtdrN8KH1yYNfFxDiW7j2Gyej8V1P5E/k5DWPW00RhGqMZpZQIDAQAB";
    AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",appId,privateKey,"JSON","utf-8",aliPublicKey,"RSA2");
    AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
    AlipayTradeRefundModel model = new AlipayTradeRefundModel();
    model.setOutTradeNo("179296933574082560");
    model.setTradeNo("2018041321001004640222635401");
    model.setRefundAmount("400.00");
    model.setRefundReason("正常退款");
    request.setBizModel(model);
    AlipayTradeRefundResponse response = alipayClient.execute(request);
    if(response.isSuccess()){
      System.out.println("调用成功");
    } else {
      System.out.println("调用失败");
    }
    System.err.println(Json.encode(response));
  }
}
