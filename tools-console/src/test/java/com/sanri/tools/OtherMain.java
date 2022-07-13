package com.sanri.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OtherMain {

    @Test
    public void test() throws IOException {
        String data = "{\"code\":1000,\"codeEnum\":\"SUCCESS\",\"data\":{\"eamUploadReturnVO\":{\"billNo\":\"TR2111130007\",\"checkRepeatResult\":{\"repeatInfos\":[]},\"collectType\":\"1\",\"createTime\":\"2022-06-02 14:51:15\",\"imageId\":\"03ac94354f20464585c8773ebac2598e\",\"invoiceOcrInspectVO\":[{\"administrativeDivisionName\":\"\",\"administrativeDivisionNo\":\"\",\"affirmTime\":null,\"agentMark\":\"\",\"amountTax\":\"\",\"appCode\":\"\",\"arrivestation\":\"\",\"attachName\":\"\",\"attachNameOrig\":\"\",\"attachmentId\":\"\",\"authenticationStatus\":null,\"bigTotalTax\":\"\",\"billingdate\":null,\"billnumber\":\"\",\"blockChain\":\"\",\"caacDevelopmentFund\":\"\",\"carCode\":\"\",\"carEngineCode\":\"\",\"carModel\":\"\",\"cargoInformation\":\"\",\"carrierName\":\"\",\"carrierTaxNo\":\"\",\"certificateNumber\":\"\",\"checkDate\":\"\",\"checkcode\":\"\",\"ciphertext\":\"\",\"city\":\"\",\"claimStatus\":null,\"codeConfirm\":\"\",\"compCode\":\"\",\"companyName\":\"\",\"companySeal\":\"\",\"companyTaxId\":\"\",\"createtime\":null,\"delete\":null,\"departurestation\":\"\",\"draweeName\":\"\",\"draweeTaxNo\":\"\",\"easVoucher\":\"\",\"eimsInvoiceTypeFields\":null,\"email\":\"\",\"endstation\":\"\",\"entrance\":\"\",\"export\":\"\",\"formName\":\"\",\"formType\":\"\",\"fromAddr\":\"\",\"id\":\"\",\"idcardNo\":\"\",\"image\":\"\",\"imageId\":\"03ac94354f20464585c8773ebac2598e\",\"importCertificateNo\":\"\",\"insepectmessage\":\"\",\"inspectionListNo\":\"\",\"insurance\":\"\",\"invoiceLineList\":null,\"invoiceResouse\":\"\",\"invoiceSourceType\":\"\",\"invoicecode\":\"\",\"invoicejson\":\"\",\"invoicenumber\":\"\",\"invoicestatus\":\"\",\"invoicetime\":\"\",\"invoicetype\":\"\",\"invoicetypecode\":\"\",\"isBill\":\"\",\"isHighlight\":\"\",\"isInspect\":\"\",\"issuer\":\"\",\"kind\":\"\",\"licensePlate\":\"\",\"machineCode\":\"\",\"machineNumber\":\"\",\"machineTaxNo\":\"\",\"mailAccountId\":\"\",\"memo\":\"\",\"mileage\":\"\",\"numberConfirm\":\"\",\"ocrmessage\":\"\",\"oilMark\":\"\",\"oliamount\":\"\",\"orientation\":\"\",\"originName\":\"发票[3-2]_merge.pdf\",\"originPlace\":\"\",\"parentImageName\":\"\",\"passengersLimited\":\"\",\"payer\":\"\",\"payerNo\":\"\",\"paymentVoucherNo\":\"\",\"pdfId\":\"\",\"pdfUrl\":\"\",\"period\":\"\",\"pid\":\"\",\"place\":\"\",\"priceamount\":\"\",\"province\":\"\",\"purchaserAddressPhone\":\"\",\"purchaserBank\":\"\",\"purchasername\":\"\",\"purchasertaxno\":\"\",\"receiptor\":\"\",\"registrationNumber\":\"\",\"reserved1\":\"\",\"reserved10\":\"\",\"reserved2\":\"\",\"reserved3\":\"\",\"reserved4\":\"\",\"reserved5\":\"\",\"reserved6\":\"\",\"reserved7\":\"\",\"reserved8\":\"\",\"reserved9\":\"\",\"reviewer\":\"\",\"salesAddressPhone\":\"\",\"salesBank\":\"\",\"salesname\":\"\",\"salestaxno\":\"\",\"scanner\":\"\",\"seattype\":\"\",\"sendTime\":\"\",\"serialNumber\":\"\",\"startstation\":\"\",\"status\":null,\"tax\":null,\"taxAuthorities\":\"\",\"taxAuthoritiesCode\":\"\",\"taxRate\":\"\",\"thumbnailUrl\":\"\",\"timeGetOff\":\"\",\"timeGetOn\":\"\",\"tollSign\":\"\",\"tonnage\":\"\",\"totalamount\":\"\",\"trainNumber\":\"\",\"tranSportRoute\":\"\",\"transitMark\":\"\",\"travelTax\":\"\",\"ttcode\":\"\",\"updTime\":null,\"updUserName\":\"\",\"usercode\":\"\",\"username\":\"\",\"vehicleType\":\"\",\"zeroTaxRateSign\":\"\"}],\"isBill\":\"3\",\"originName\":\"发票[3-2]_merge.pdf\"},\"errorFiles\":[],\"success\":2,\"total\":2},\"message\":\"运行成功\"}";
//        final JSONObject jsonObject = JSON.parseObject(data);
//        final String s = JSONObject.toJSONString(jsonObject, SerializerFeature.WriteMapNullValue);
//        System.out.println(s);
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        final Map map = objectMapper.readValue(data, Map.class);
        final Map data1 = (Map) map.get("data");
        final Map eamUploadReturnVO = (Map) data1.get("eamUploadReturnVO");
        Iterator iterator = eamUploadReturnVO.entrySet().iterator();
        while (iterator.hasNext()){
            final Map.Entry next = (Map.Entry) iterator.next();
            if ("".equals(next.getValue())){
                iterator.remove();
            }
        }
        final Map invoiceOcrInspectVO = (Map) ((List)(eamUploadReturnVO.get("invoiceOcrInspectVO"))).get(0);
        iterator = invoiceOcrInspectVO.entrySet().iterator();
        while (iterator.hasNext()){
            final Map.Entry next = (Map.Entry) iterator.next();
            if ("".equals(next.getValue())){
                iterator.remove();
            }
        }
        final String s = objectMapper.writeValueAsString(map);
        System.out.println(s);
    }
}
