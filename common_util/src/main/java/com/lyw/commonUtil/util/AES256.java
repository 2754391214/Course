package com.lyw.commonUtil.util;

import cn.hutool.core.codec.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;


public class AES256 {
    private static final Logger log = LoggerFactory.getLogger(AES256.class);

    private static final String AES_KEY = "0123456789abcdef0123456789abcdef";


    public static String getKey(){
        return Hex.byte2Hex(Hex.tohash256Deal(AES_KEY)).substring(0, 32);
    }



    public static String encrypt(String content, String password) {
        try {
            /**
             * CREATE DEFINER=`root`@`localhost` FUNCTION `ESAM_ENTRY`(email VARCHAR(100)) RETURNS varchar(100) CHARSET utf8
             * BEGIN
             *     SET block_encryption_mode = 'aes-256-ecb';
             * 	SET @key_str = substring(CAST(SHA2('0123456789abcdef0123456789abcdef', 256) AS CHAR CHARACTER SET utf8),1 ,32);
             *     RETURN CONVERT(to_base64(AES_ENCRYPT(email, @key_str)), CHAR CHARACTER SET utf8);
             * END
             */
//            removeCryptographyRestrictions();
            /**
             *  KeyGenerator : 是个类，此类提供（对称）密钥生成器的功能。在生成密钥后，可以重复使用同一个 KeyGenerator 对象来生成更多的密钥。
             *  使用其中的getInstance（String algorithm）方法进行构造对象；
             *  algorithm ： 所请求密钥算法的标准名称。
             */
//            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            /**
             * Random : 此类的实例用于生成伪随机数流。
             * SecureRandom : 是Random的直接子类。此类提供强加密随机数生成器 (RNG)。还必须生成非确定性输出（而）。
             * SecureRandom(byte[] seed) :  构造一个实现默认随机数算法的安全随机数生成器 (RNG)。使用指定的种子字节设置种子。
             * seed : 种子。在这儿用的是一个存放哈希结果的数组，详细看下文的tohash256Deal(String datastr)。
             */
//            SecureRandom securerandom = new SecureRandom(tohash256Deal(password));
//            SecureRandom securerandom = SecureRandom.getInstance("SHA1PRNG");
//            securerandom.setSeed(Hex.tohash256Deal(password));
            /**
             * init(int keysize, SecureRandom random) : 使用用户提供的随机源初始化此密钥生成器，使其具有确定的密钥大小。
             * keysize : 密钥大小。这是特定于算法的一种规格，是以位数为单位指定的。
             * random : 此密钥生成器的随机源。
             */
//            kgen.init(256, securerandom);
            /**
             * java.security包中有接口 Key，SecretKey是Key的子接口，SecretKeySpec是SecretKey的实现类。
             * Key : Key 是所有密钥的顶层接口。它定义了供所有密钥对象共享的功能。
             * SecretKey : 此接口不包含方法或常量。其唯一目的是分组秘密密钥（并为其提供类型安全）。
             * SecretKeySpec : 可以使用此类来根据一个字节数组构造一个 SecretKey
             *
             * generateKey() : 生成一个密钥。
             */
//            SecretKey secretKey = kgen.generateKey();
            /**
             * getEncoded() : 是Key接口中的方法；返回基本编码格式的密钥，如果此密钥不支持编码，则返回 null。
             */
//            byte[] enCodeFormat = secretKey.getEncoded();
            /**
             * SecretKeySpec(byte[] key, String algorithm) : SecretKeySpec的构造方法之一，根据给定的字节数组构造一个密钥。
             * key : 密钥的密钥内容。复制该数组的内容来防止后续修改。
             * algorithm : 与给定的密钥内容相关联的密钥算法的名称。
             */
            SecretKeySpec key = new SecretKeySpec(password.getBytes(StandardCharsets.UTF_8), "AES");
            /**
             * Security : 此类集中了所有的安全属性和常见的安全方法。其主要用途之一是管理提供程序。
             * addProvider(Provider provider) : 将提供程序添加到下一个可用位置。
             * provider : 要添加的提供程序。
             */
//            Security.addProvider(new BouncyCastleProvider());
            /**
             * Cipher : 该类为加密和解密提供加密密码功能。
             * getInstance(String transformation, String provider) : 创建一个实现指定转换的 Cipher 对象，该转换由指定的提供程序提供。
             * transformation : 转换的名称，例如 DES/CBC/PKCS5Padding。描述为产生某种输出而在给定的输入上执行的操作（或一组操作）的字符串。转换始终包括加密算法的名称（例如，DES），后面可能跟有一个反馈模式和填充方案。
             * provider : 提供程序的名称
             */
//            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            /**
             * init(int opmode, Key key) : 用密钥初始化此 cipher。为以下 4 种操作之一初始化该 cipher：加密、解密、密钥包装或密钥打开，这取决于 opmode 的值。
             * opmode : 此 cipher 的操作模式（其为如下之一：ENCRYPT_MODE、DECRYPT_MODE、WRAP_MODE 或 UNWRAP_MODE）
             * key : 密钥
             */
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] byteContent = content.getBytes("utf-8");
            /**
             * doFinal(byte[] input) : 按单部分操作加密或解密数据，或者结束一个多部分操作。数据被加密还是解密取决于此 cipher 的初始化方式。
             * input : 输入缓冲区
             */
            byte[] cryptograph = cipher.doFinal(byteContent);
            /**
             * ！注：在这儿，加密后的byte数组是不能强制转换成字符串的(即：new String（result）); 换言之,字符串和byte数组在这种情况下不是互逆的。
             * 处理方式有两种：
             * 	1.将result转化为十六进制的数据再做处理（需要自己写一个转换方法）—— 用法和“128位密钥的加解密算法”的一模一样，可以参考上文；
             * 	2.将result进行Base64(也可以用 BASE64Encode)再次加密在进行强制转换（不需要自己写方法，省事儿）。（主要编解码方式有Base64, HEX, UUE, 7bit等等。此处看服务器需要什么编码方式）
             */
            return new String(Base64.encode(cryptograph));
        } catch (Exception e) {
            log.info("AES encrypt fail: {}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    public static String decrypt(String cryptograph, String password) {
        try {
            /**
             * CREATE DEFINER=`root`@`localhost` FUNCTION `ESAM_DETRY`(email VARCHAR(100)) RETURNS varchar(100) CHARSET utf8
             * BEGIN
             * 	SET block_encryption_mode = 'aes-256-ecb';
             * 	SET @key_str = substring(CAST(SHA2('0123456789abcdef0123456789abcdef', 256) AS CHAR CHARACTER SET utf8),1 ,32);
             * 	RETURN CAST(AES_DECRYPT(from_base64(email), @key_str) AS CHAR CHARACTER SET utf8);
             * END
             */
            SecretKeySpec key = new SecretKeySpec(password.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] content = cipher.doFinal(Base64.decode(cryptograph.getBytes()));
            return new String(content,"utf-8");
        } catch (Exception e) {
            log.info("AES decrypt fail: {}", e.getMessage());
            e.printStackTrace();
//            throw new Exception("key decrypt fail: "+cryptograph);
        }
        return null;
    }

    /**
     * 解密字符串
     * @param content
     * @return
     */
    public static String decrypt(String content) {
        return decrypt(content,getKey());
    }

    /**
     * 解密密文
     * @param content
     * @return
     */
    public static String encrypt(String content) {
        return encrypt(content,getKey());
    }

    /**
     *
     *
     select encrypt_function('fishhead')

     select decrypt_function('BvwNdU+dCorOU0D7E1kNxO9S7pX2cfKRAU0RRY3ffJu6pVRIibcNQmWpFHatGGh8HC3zTJxnQdX2tZpMPTmKuA==')

     select substring('0123456789abcdef0123456789abcdef',1 ,16);

     select substring(CAST(SHA2('0123456789abcdef0123456789abcdef', 256) AS CHAR CHARACTER SET utf8),1 ,32);
     */

    public static void main(String[] args) throws Exception {
        System.out.println(AES256.encrypt("123", AES256.getKey()));
        System.out.println(AES256.decrypt("5xvP3FPCa8+CYx/BxURgdw==", AES256.getKey()));
    }
}

