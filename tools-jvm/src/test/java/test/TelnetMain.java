package test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.telnet.TelnetClient;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;

public class TelnetMain {
    @Test
    public void test1() throws IOException {
        TelnetClient client = new TelnetClient();
        client.setConnectTimeout(2000);
        client.connect("192.168.61.71", 3668);
        final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), Charsets.UTF_8));
        bufferedWriter.write("sc");
        bufferedWriter.newLine();
        bufferedWriter.flush();

        final InputStream inputStream = client.getInputStream();
        final String s = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        System.out.println(s);
    }

    @Test
    public void test2(){
        final JSONObject jsonObject = JSONObject.parseObject("{\"isBill\":\"3\",\"isEnd\":\"0\",\"billNo\":\"HTQSSQ20211101006786\",\"transferCount\":null,\"imageId\":\"589afc06759940d2adc2b04879d14868\",\"originName\":\"scan1642660928300.jpg\",\"uploadStatus\":1000,\"collectType\":\"1\",\"checkStatusDesc\":\"上传成功\",\"invoiceOcrInspectVO\":[{\"id\":null,\"invoicetype\":null,\"invoicetypecode\":null,\"invoicecode\":null,\"invoicenumber\":null,\"totalamount\":null,\"billingdate\":null,\"checkcode\":null,\"appCode\":null,\"ttcode\":null,\"image\":null,\"invoicejson\":null,\"invoicestatus\":null,\"billnumber\":null,\"scanner\":null,\"createtime\":null,\"ocrmessage\":null,\"insepectmessage\":null,\"seattype\":null,\"endstation\":null,\"startstation\":null,\"usercode\":null,\"username\":null,\"oliamount\":null,\"priceamount\":null,\"invoicetime\":null,\"entrance\":null,\"export\":null,\"timeGetOn\":null,\"timeGetOff\":null,\"mileage\":null,\"place\":null,\"departurestation\":null,\"arrivestation\":null,\"imageId\":\"589afc06759940d2adc2b04879d14868\",\"amountTax\":null,\"orientation\":null,\"salestaxno\":null,\"salesname\":null,\"purchasertaxno\":null,\"purchasername\":null,\"purchaserBank\":null,\"purchaserAddressPhone\":null,\"salesAddressPhone\":null,\"salesBank\":null,\"tax\":null,\"taxRate\":null,\"machineCode\":null,\"machineNumber\":null,\"taxAuthorities\":null,\"taxAuthoritiesCode\":null,\"carCode\":null,\"carEngineCode\":null,\"carModel\":null,\"certificateNumber\":null,\"companyName\":null,\"companyTaxId\":null,\"licensePlate\":null,\"registrationNumber\":null,\"trainNumber\":null,\"caacDevelopmentFund\":null,\"insurance\":null,\"carrierName\":null,\"carrierTaxNo\":null,\"draweeName\":null,\"draweeTaxNo\":null,\"cargoInformation\":null,\"tranSportRoute\":null,\"machineTaxNo\":null,\"tonnage\":null,\"memo\":null,\"originPlace\":null,\"inspectionListNo\":null,\"importCertificateNo\":null,\"paymentVoucherNo\":null,\"passengersLimited\":null,\"tollSign\":null,\"zeroTaxRateSign\":null,\"invoiceResouse\":null,\"compCode\":null,\"easVoucher\":null,\"updTime\":null,\"updUserName\":null,\"thumbnailUrl\":null,\"attachmentId\":null,\"authenticationStatus\":null,\"affirmTime\":null,\"period\":null,\"claimStatus\":null,\"checkDate\":null,\"bigTotalTax\":null,\"status\":null,\"vehicleType\":null,\"companySeal\":null,\"receiptor\":null,\"reviewer\":null,\"issuer\":null,\"transitMark\":null,\"oilMark\":null,\"blockChain\":null,\"kind\":null,\"formType\":null,\"formName\":null,\"province\":null,\"city\":null,\"agentMark\":null,\"ciphertext\":null,\"travelTax\":null,\"codeConfirm\":null,\"numberConfirm\":null,\"attachName\":null,\"attachNameOrig\":null,\"fromAddr\":null,\"sendTime\":null,\"mailAccountId\":null,\"email\":null,\"pdfUrl\":null,\"invoiceLineList\":null,\"serialNumber\":null,\"administrativeDivisionNo\":null,\"administrativeDivisionName\":null,\"idcardNo\":null,\"invoiceSourceType\":null,\"isInspect\":null,\"reserved1\":null,\"reserved2\":null,\"reserved3\":null,\"reserved4\":null,\"reserved5\":null,\"reserved6\":null,\"reserved7\":null,\"reserved8\":null,\"reserved9\":null,\"reserved10\":null,\"eimsInvoiceTypeFields\":null,\"originName\":\"scan1642660928300.jpg\",\"pid\":null,\"pdfId\":null,\"isHighlight\":null,\"parentImageName\":null,\"isBill\":\"3\"}]}");
        final String s = JSON.toJSONString(jsonObject);
        System.out.println(s);
    }

    /**
     * arthas 远程调用
     */
    @Test
    public void test14() throws IOException, InterruptedException {
        final TelnetClient telnetClient = new TelnetClient();
        telnetClient.connect("192.168.61.71",9999);
        final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(telnetClient.getOutputStream(), Charsets.UTF_8));
        bufferedWriter.write("sc com.sanri.*");
        bufferedWriter.newLine();
        bufferedWriter.flush();

        Runnable runnable = () -> {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(telnetClient.getInputStream(), StandardCharsets.UTF_8));
            while (true){
                String line = null;
                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                    }
                }catch (IOException e){
                    System.out.println("读取数据异常:"+e.getMessage());
                }
            }
        };
        final Thread thread = new Thread(runnable);
        thread.start();
        thread.join();
    }
}
