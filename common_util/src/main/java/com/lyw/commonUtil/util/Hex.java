package com.lyw.commonUtil.util;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hex {
    public static String encodeHexString(byte[] initKey) {
        StringBuffer stb = new StringBuffer();
        for(byte b: initKey){
            String sb = Integer.toHexString(b);
            if(sb.length()>3){
                stb.append(sb.substring(6));
            }else{
                if(sb.length()<2){
                    stb.append("0"+sb);
                }else{
                    stb.append(sb);
                }
            }
//            stb.append(" ");
        }
        return stb.toString();
    }

    /**
     * 将byte转为16进制
     * @param bytes
     * @return
     */
    public static String byte2Hex(byte[] bytes){
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i=0;i<bytes.length;i++){
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length()==1){
                //1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    public static byte[] decodeHex(char[] toCharArray) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(toCharArray.length);
        for (char c : toCharArray) {
            bout.write(c);
        }
        return bout.toByteArray();
    }


    public static byte[] tohash256Deal(String datastr) {
        try {
            /**
             * MessageDigest : 该类是个抽象类，此类为应用程序提供信息摘要算法的功能，如 MD5 或 SHA 算法。信息摘要是安全的单向哈希函数，它接收任意大小的数据，输出固定长度的哈希值。
             * getInstance(String algorithm) ： 生成实现指定摘要算法的 MessageDigest 对象。
             * algorithm : 所请求算法的名称。
             * 该对象通过使用 update 方法处理数据。任何时候都可以调用 reset 方法重置摘要。一旦所有需要更新的数据都已经被更新了，应该调用 digest 方法之一完成哈希计算。
             */
            MessageDigest digester=MessageDigest.getInstance("SHA-256");
            /**
             * update(byte[] input) : 使用指定的字节数组更新摘要。
             */
            digester.update(datastr.getBytes());
            /**
             * digest() : 通过执行诸如填充之类的最终操作完成哈希计算。调用此方法后摘要被重置。
             */
            byte[] hex=digester.digest();
            /**
             * hex : 存放哈希值结果的字节数组。
             */
            return hex;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
