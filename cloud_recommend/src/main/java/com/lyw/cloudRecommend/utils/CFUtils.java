package com.lyw.cloudRecommend.utils;



import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class CFUtils {
    @Value(value = "${recommend.file.path}")
    public static String path = "/usr/local/docker/my/";
    static Map<String,Integer> itemIDMap = new HashMap<String,Integer>();//课程ID列表
    static Map<Integer,String> idToItemMap = new HashMap<Integer,String>();//课程ID转课程原名称
    static Map<String,Integer> userIDMap = new HashMap<String,Integer>();//用户ID列表
    static Map<Integer,String> idToUserMap = new HashMap<Integer,String>();//用户ID转用户原名称
    static Map<String,HashMap<String,Double>> userMap = new HashMap<String,HashMap<String, Double>>(); //记录用户对于课程的评分
    static double[][] simMatrix; //用户之间的相似矩阵
    static int TOP_K = 25;  //选择的近邻数量
    static int TOP_N = 20;  //定义最长推荐列表
    public static void recommendCourse() throws IOException {
        readUI(); //读取user-item交互数据
        user_distance(); //计算用户相似性
        recommend(); //产生推荐
    }
    public static void readUI() throws IOException{

        String uiFile =path+"input.txt";
        BufferedReader  bfr_ui = new BufferedReader(new InputStreamReader(new FileInputStream(new File(uiFile)),"UTF-8"));
        String line;
        String[] SplitLine;

        int itemId = 0; //产品计数
        int userId = 0;//用户计数
        while((line = bfr_ui.readLine()) != null){
            SplitLine = line.split(" ");
            //如果不包含当前产品，存入map中
            if(!itemIDMap.containsKey(SplitLine[1])) {
                itemIDMap.put(SplitLine[1], itemId);
                idToItemMap.put(itemId, SplitLine[1]);

                itemId ++;
            }
            //如果不包含当前的用户，存入map中
            if(!userMap.containsKey(SplitLine[0])) {
                userIDMap.put(SplitLine[0], userId);
                idToUserMap.put(userId, SplitLine[0]);
                userId++;
                //新建Map用于存储当前用户的评分列表
                HashMap<String, Double> curentUserMap = new HashMap<String,Double>();
                //将当前用户评分加入当前评分列表中
                curentUserMap.put(SplitLine[1], Double.parseDouble(SplitLine[2]));
                userMap.put(SplitLine[0], curentUserMap);
            }else { //如果已存在当前用户，将该用户先前的评分拿出来，再加入新的评分
                HashMap<String, Double> curentUserMap = userMap.get(SplitLine[0]);
                curentUserMap.put(SplitLine[1], Double.parseDouble(SplitLine[2]));
                userMap.put(SplitLine[0], curentUserMap);
            }
        }
    }
    //获取用户之间的相似相似性
    public static void user_distance() {
        //初始化用户相似矩阵
        simMatrix = new double[userMap.size()][userMap.size()];

        //循环每个用户计算相似性
        for(Map.Entry<String, HashMap<String,Double>> userEntry_1 : userMap.entrySet()) {
            //先获得该用户的评分列表,转化为评分数组
            double[] ratings_1 = new double[itemIDMap.size()]; //初始化评分数组，长度为所有item的数量
            for(Map.Entry<String, Double> itemEntry : userEntry_1.getValue().entrySet()) {
                ratings_1[itemIDMap.get(itemEntry.getKey())] = itemEntry.getValue();//用评分进行赋值
            }
            //循环其他用户
            for(Map.Entry<String, HashMap<String,Double>> userEntry_2 : userMap.entrySet()) {
                //对于其他用户,首先判断用户的id十分大于前面的用户，1-2，1-3，2-3，避免重复计算相似性
                if(userIDMap.get(userEntry_2.getKey())>userIDMap.get(userEntry_1.getKey())) {
                    //同样获得该用户的评分列表,转化为评分数组
                    double[] ratings_2 = new double[itemIDMap.size()]; //初始化评分数组，长度为所有item的数量
                    for(Map.Entry<String, Double> itemEntry : userEntry_2.getValue().entrySet()) {
                        ratings_2[itemIDMap.get(itemEntry.getKey())] = itemEntry.getValue();//用评分进行赋值
                    }
                    //根据两个用户的评分数组，计算二者的相似性，这里使用欧式距离,sim =
                    double square_sum = 0; //定义平方和
                    double similarity = 0; //定义相似性
                    for(int i = 0;i<ratings_1.length;i++) {
                        square_sum +=Math.pow((ratings_1[i]-ratings_2[i]), 2); //差值的平方
                    }
                    similarity = 1/(1+Math.sqrt(square_sum));
                    simMatrix[userIDMap.get(userEntry_1.getKey())][userIDMap.get(userEntry_2.getKey())] = similarity;
                    simMatrix[userIDMap.get(userEntry_2.getKey())][userIDMap.get(userEntry_1.getKey())] = similarity;//根据相似的对称性
                }
            }
        }
    }
    //根据用户的相似性为每个用户产生推荐列表
    public static void recommend() throws IOException{
        String resultFile = path+"result.txt";
        BufferedWriter bfw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(resultFile)),"UTF-8"));

        //将每个用户的相似关系存入map，再进行排序，选取前TOP_K个相似的用户
        for(int i = 0;i<userMap.size();i++) {
            //将其他用户与当前用户的相似性存入map中用于排序
            Map<Integer,Double> simMap = new HashMap<Integer,Double>();
            Map<String,Double> preRatingMap = new HashMap<String,Double>();

            for(int j = 0;j<userMap.size();j++) {
                simMap.put(j, simMatrix[i][j]);
            }
            simMap = sortMapByValues(simMap);
            //选取前TOP_K个相似的用户
            int userCount = 0;
            ArrayList<Integer> simUserList = new ArrayList<Integer>();
            for(Map.Entry<Integer, Double> entry : simMap.entrySet()) {
                if(userCount <TOP_K)
                    simUserList.add(entry.getKey());
                userCount ++;
            }
            //获得最相似的K个近邻后，统计近邻用户喜欢过，但是用户没有推荐的课程
            //当前用户产品列表
            HashSet<String> currentUserSet = new HashSet<String>();
            for(Map.Entry<String, Double> entry :userMap.get(idToUserMap.get(i)).entrySet()) {
                currentUserSet.add(entry.getKey());
            }
            //获取好友的课程列表和自己课程列表集合
            HashSet<String> currentFriendSet = new HashSet<String>(); //好友评价过的item
            for(int user : simUserList) {
                for(Map.Entry<String, Double> entry :userMap.get(idToUserMap.get(user)).entrySet()) {
                    currentFriendSet.add(entry.getKey());
                }
            }
            //获取当前用户外的课程列表
            HashSet<String> unRatingSet = new HashSet<String>();
            for(String item : currentFriendSet) {
                if(!currentUserSet.contains(item)) {
                    unRatingSet.add(item);
                }
            }
            //如果没有当前用户外的课程列表，直接跳过
            if(unRatingSet.isEmpty())
                continue;
            //如果列表不为空的话，将当前用户存入
            bfw.append(idToUserMap.get(i)+" ");

            //对于好友的课程列表的产品进行评分预测
            for(String item : unRatingSet) {
                double totalRating = 0;
                double totalSim = 0;
                double preRating = 0;

                //找出评价了这个item的用户,并记录打分
                for(int user : simUserList) {

                    //获取当前用户user的课程集 item--rating
                    for(Map.Entry<String, Double> entry : userMap.get(idToUserMap.get(user)).entrySet()) {
                        if(entry.getKey() == item) {
                            totalRating += entry.getValue()*simMatrix[i][user];
                            totalSim += simMatrix[i][user];
                        }
                    }
                    //得到产品的预测评分，存入map中
                    preRating = totalRating/totalSim;
                    preRatingMap.put(item, preRating);

                }
            }

            //将推荐结果进行排序
            preRatingMap = sortMapByValues(preRatingMap);
            //推荐TOP_N个产品
            int recCount = 0;
            for(Map.Entry<String, Double> entry : preRatingMap.entrySet()) {
                if(recCount < TOP_N) {
                    bfw.append(entry.getKey() + " ");
                    recCount ++;
                    bfw.flush();
                }
            }

            bfw.newLine();
            bfw.flush();
        }
        bfw.flush();
        bfw.close();
    }
    //对map进行从大到小排序
    public static <K extends Comparable, V extends Comparable> Map<K, V> sortMapByValues(Map<K, V> aMap) {
        HashMap<K, V> finalOut = new LinkedHashMap<>();
        aMap.entrySet().stream().sorted((p1, p2) -> p2.getValue().compareTo(p1.getValue())).collect(Collectors.toList())
                .forEach(ele -> finalOut.put(ele.getKey(), ele.getValue()));
        return finalOut;
    }
}


