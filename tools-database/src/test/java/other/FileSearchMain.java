package other;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileSearchMain {
    List<String> clientLogs = new ArrayList<>();
    List<String> backLogs = new ArrayList<>();

    @Before
    public void init() throws IOException {
        backLogs = FileUtils.readLines(new File("d:/test/anta/utax.log"), StandardCharsets.UTF_8);
        clientLogs = FileUtils.readLines(new File("d:/test/anta/utaxclient0.log.0"),StandardCharsets.UTF_8);
    }

    Pattern beginUploadLineRegex = Pattern.compile("\\d{13}\\sparamMap");
    Pattern fileNameRegex = Pattern.compile("scan\\d{13}\\.jpg");
    Pattern batchNumberRegex = Pattern.compile("upload:\\s(\\d{13})\\sparamMap");
    Pattern threadRegex = Pattern.compile("\\[(http-nio-8280-exec-\\d+)\\]");
    Pattern pythonRequestRegex = Pattern.compile("parameters:\\{\\\"Bin\\\"\\:\\\"Base64\\\"");
    Pattern pythonResponseRegex = Pattern.compile("OCR分类器返回数据：");
    Pattern uploadEndTimeRegex = Pattern.compile("eamUploadReturnVO.getImageId");

    @Test
    public void test3() throws ParseException {
        final Date date = DateUtils.parseDate("2022-02-28 10:28:03", "yyyy-MM-dd HH:mm:ss,S", "yyyy-MM-dd HH:mm:ss");
        System.out.println(date);
    }

    @Test
    public void test4(){
        for (String backLog : backLogs) {
            if (pythonResponseRegex.matcher(backLog).find()){
                System.out.println(backLog);
            }
        }
    }

    @Test
    public void test1() throws IOException {
        File file = new File("d:/test/result.csv");
        final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));

        for (String clientLog : clientLogs) {
            if (beginUploadLineRegex.matcher(clientLog).find()){
                final Matcher batchNumberGroup = batchNumberRegex.matcher(clientLog);
                String batchNumber = null;
                if (batchNumberGroup.find()){
                    batchNumber = batchNumberGroup.group(1);
                }

                final Matcher fileNameMatch = fileNameRegex.matcher(clientLog);
                String fileName = null;
                if (fileNameMatch.find()){
                    fileName = fileNameMatch.group(0);
                }
                String uploadTime = clientLog.substring(0,20);
//                System.out.println(batchNumber + " : "+fileName + " : "+ uploadTime);
                Date uploadTimeDate = null;
                try{
                    uploadTimeDate = DateUtils.parseDate(uploadTime.trim(), "yyyy-MM-dd'T'HH:mm:ss","yyyy-MM-dd HH:mm:ss");
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }

                // 根据 fileName 去第二个文件找
                Pattern reciveFileRegex = Pattern.compile("文件名："+fileName);
                for (int i = 0; i < backLogs.size(); i++) {
                    final String backLog = backLogs.get(i);

                    if (reciveFileRegex.matcher(backLog).find()){
                        String reciveTime = backLog.substring(5,28).replaceAll(",",".");
                        long networkTime = 0;
                        try{
                            if (uploadTime != null){
                                final Date reciveTimeDate = DateUtils.parseDate(reciveTime, "yyyy-MM-dd HH:mm:ss.S","yyyy-MM-dd HH:mm:ss");
                                networkTime = reciveTimeDate.getTime() - uploadTimeDate.getTime();
                            }
                        }catch (Exception e){
                            System.out.println(e.getMessage());
                        }
                        String threadName = null;
                        final Matcher matcher = threadRegex.matcher(backLog);
                        if (matcher.find()){
                            threadName = matcher.group(1);
                        }
                        if (StringUtils.isBlank(threadName)){
                            System.out.println("线程名为空: "+backLog);
                            continue;
                        }

//                        System.out.println(reciveTime+":"+threadName);
                        String pythonStartTime = null;
                        String pythonEndTime = null;
                        int j = i;
                        for (;j < backLogs.size(); j++) {
                            final String secondBackLog = backLogs.get(j);
                            if (pythonRequestRegex.matcher(secondBackLog).find() && secondBackLog.contains(threadName)){
                                pythonStartTime = secondBackLog.substring(5,28).replaceAll(",",".");
                                break;
                            }
                        }
                        int k = j;
                        for (; k < backLogs.size(); k++) {
                            final String thirdBackLog = backLogs.get(k);
                            if (pythonResponseRegex.matcher(thirdBackLog).find() && thirdBackLog.contains(threadName)){
                                pythonEndTime = thirdBackLog.substring(5,28).replaceAll(",",".");
                                break;
                            }
                        }

                        String uploadEndTime = null;
                        for (int m = k; m < backLogs.size(); m++) {
                            final String fourBackLog = backLogs.get(m);
                            if (uploadEndTimeRegex.matcher(fourBackLog).find() && fourBackLog.contains(threadName)){
                                uploadEndTime = fourBackLog.substring(5,28).replaceAll(",",".");
                                break;
                            }
                        }

                        long allSpendTime = 0;
                        try{
                            if (StringUtils.isNotBlank(uploadEndTime)) {
                                final Date uploadEndTimeDate = DateUtils.parseDate(uploadEndTime, "yyyy-MM-dd HH:mm:ss.S", "yyyy-MM-dd HH:mm:ss");
                                allSpendTime = uploadEndTimeDate.getTime() - uploadTimeDate.getTime();
                            }
                        }catch (Exception e){
                            System.out.println(e.getMessage());
                        }

                        long pythonHandlerTime = 0 ;
                        try{
                            final Date pythonStartTimeDate = DateUtils.parseDate(pythonStartTime, "yyyy-MM-dd HH:mm:ss.S","yyyy-MM-dd HH:mm:ss");
                            final Date pythonEndTimeDate = DateUtils.parseDate(pythonEndTime, "yyyy-MM-dd HH:mm:ss.S","yyyy-MM-dd HH:mm:ss");
                            pythonHandlerTime = pythonEndTimeDate.getTime() - pythonStartTimeDate.getTime();
                        }catch (Exception e){
                            System.out.println(e.getMessage());
                        }

                        String result = "批次:"+batchNumber+" 客户端上传时间:"+uploadTime + "后端接收时间: "+reciveTime + " 网络耗时: "+networkTime+" ms"  +" 文件名: "
                                +fileName+" python 请求时间:"+pythonStartTime+" python 处理结束时间:"+pythonEndTime + " python 处理时间:"+pythonHandlerTime+" ms" + " 总花费时间:"+allSpendTime +" ms" ;
                        System.out.println(result);
                        String [] items = {batchNumber,fileName,uploadTime,reciveTime,networkTime+"",pythonStartTime,pythonEndTime,pythonHandlerTime+"","否","0ms~900ms",uploadEndTime,allSpendTime+""};
                        bufferedWriter.write(StringUtils.join(items,","));
                        bufferedWriter.write('\n');

                        if (i % 500 == 0){
                            bufferedWriter.flush();
                        }
                    }

                }
            }
        }

        bufferedWriter.flush();
        bufferedWriter.close();
    }

    @Test
    public void test2(){
        Pattern reciveFileRegex = Pattern.compile("文件名：scan1646015597749.jpg");
        for (String backLog : backLogs) {
            if (reciveFileRegex.matcher(backLog).find()){
                System.out.println(backLog);
                String reciveTime = backLog.substring(0,24);
                String threadName = null;
                final Matcher matcher = threadRegex.matcher(backLog);
                if (matcher.find()){
                    threadName = matcher.group(1);
                }

                System.out.println(reciveTime+":"+threadName);
            }
        }
    }
}
