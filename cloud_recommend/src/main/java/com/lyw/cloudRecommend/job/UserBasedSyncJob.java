package com.lyw.cloudRecommend.job;

import com.lyw.cloudRecommend.mapper.RecommendResultsDao;
import com.lyw.cloudRecommend.mapper.UserCFDao;
import com.lyw.cloudRecommend.utils.CFUtils;
import com.lyw.cloudRecommend.vo.UserCFVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

@Component
public class UserBasedSyncJob {
    @Value(value = "${recommend.file.path}")
    public static String path = "/usr/local/docker/my/";
    @Resource
    private RecommendResultsDao recommendResultsDao;
    @Resource
    private UserCFDao userCFDao;

    // 每隔一分钟执行一次
    @Scheduled(cron = "0 0 0 * * ?")
    //@Scheduled(cron="0 */1 * * * ?")
    public void run() throws IOException {
        final List<UserCFVo> usercfs = userCFDao.selectList(null); //构建Input文件
        writeToFile(usercfs);
        CFUtils.recommendCourse(); //构建Result文件
        readFromFile();
    }

    public void readFromFile() {
        String filePath = path + "result.txt";
        File file = new File(filePath);
        // 定义 BufferedReader 对象
        BufferedReader br = null;
        List<UserCFVo> list = new LinkedList<>();
        try {
            // 创建 FileReader 对象
            FileReader fr = new FileReader(file);
            // 将 FileReader 对象作为参数传入 BufferedReader 构造方法
            br = new BufferedReader(fr);
            // 定义变量，用于保存当前读取到的行数据
            String line;
            // 循环读取文件每一行的数据
            while ((line = br.readLine()) != null) {
                String id = line.substring(0, line.indexOf(" "));
                String courseId = line.substring(line.indexOf(" ") + 1, line.lastIndexOf(" "));
                UserCFVo result = new UserCFVo();
                result.setUserId(Long.valueOf(id));
                result.setCourseId(Long.valueOf(courseId));
                list.add(result);
            }
            recommendResultsDao.insertOrUpdata(list);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    // 关闭 BufferedReader
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void writeToFile(List<UserCFVo> courseList) throws IOException {
        // 定义本地文件路径
        String filePath = path + "input.txt";
        // 创建 File 对象
        File file = new File(filePath);
        // 如果文件不存在则创建文件
        if (!file.exists()) {
            file.createNewFile();
        }
        // 定义 FileWriter 对象
        FileWriter fw = null;
        // 遍历 List，将每个对象写入文件
        try {
            fw = new FileWriter(file);
            for (UserCFVo usercf : courseList) {
                // 将一个对象转为字符串形式
                String courseString = usercf.toString();
                // 将字符串写入文件，每一行代表一个对象
                fw.write(courseString + System.lineSeparator());
            }
            fw.flush(); // 刷新缓存，将数据写入文件
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
