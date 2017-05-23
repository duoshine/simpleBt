package chenanduo.bluetoothconnect.util;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by chen on 2017
 */

public class Util {
    /**
     * @param b byte[] byte[] b = { 0x12, 0x34, 0x56 };
     * @return String 123456
     */
    public static String Bytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase() + " ";
        }
        return ret;
    }

    /**
     * 用于数据转换 不是偶数在前面补0 将指定字符串src，以每两个字符分割转换为16进制形式 如："2B44EFD9" -->
     * byte[]{0x2B, 0x44, 0xEF, 0xD9}
     *
     * @param src String
     * @return byte[]
     */
    public static byte[] HexString2Bytes(String src) {
        if (src.length() % 2 == 1) {
            src = "0" + src;
        }
        byte[] ret = new byte[src.length() / 2];
        byte[] tmp = src.getBytes();
        for (int i = 0; i < src.length() / 2; i++) {
            ret[i] = unionBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    /*
    用于crc16效验  可能效验后长度为1  导致越界  不足补0x00
     */
    public static byte[] hexString2Byte(String src) {
        if (src.length() % 2 == 1) {
            src = "0" + src;
        }
        byte[] ret = new byte[src.length() / 2];
        byte[] tmp = src.getBytes();
        for (int i = 0; i < src.length() / 2; i++) {
            ret[i] = unionBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        if (ret.length != 2) {
            byte[] bytes = new byte[2];
            bytes[0] = ret[0];
            bytes[1] = 0x00;
            return bytes;
        }
        return ret;
    }

    /**
     * 将两个ASCII字符合成一个字节； 如："EF"--> 0xEF
     *
     * @param src0 byte
     * @param src1 byte
     * @return byte
     */
    public static byte unionBytes(byte src0, byte src1) {
        byte b0 = Byte.decode("0x" + new String(new byte[]{src0}))
                .byteValue();
        b0 = (byte) (b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1}))
                .byteValue();
        byte ret = (byte) (b0 ^ _b1);
        return ret;
    }

    public static String Bytes2HexString_noblack(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    /*
    * 将16进制数字解码成字符串,适用于所有字符（包括中文）
    */
    public static String decode(String bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
        //将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());
    }

    /*
    * 将字符串编码成16进制数字,适用于所有字符（包括中文）
    * ZJTT-03-L000 -->5A 4A 54 54 2D 30 33 2D 4C 30 30 30
    */
    private static String hexString = "0123456789ABCDEF";

    public static String encode(String str) {
        //根据默认编码获取字节数组
        byte[] bytes = str.getBytes();
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        //将字节数组中每个字节拆解成2位16进制整数
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
            sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
        }
        return sb.toString();
    }

    /**
     * 数组复制
     */
    public void arraycopy() {
        int[] src = {1, 2, 3, 4};
        int[] dest = {1, 2, 3, 0, 0, 0, 0};
        /**
         * 从src数组的0位置开始赋值 目标数组是dest,目的地位置是dest数组的3，长度是复制src整个数组
         * 也就是复制的长度是4，注意，复制长度不能大于src的长度亦不能大于dest的目的地位置之后剩余的长度
         * 否则越界
         */
        System.arraycopy(src, 0, dest, 3, src.length);
        //运行结果: dest = {1,2,3,1,2,3,4}
    }


    /**
     * 生成随机数
     *
     * @return
     */
    public static byte[] getRandomByte(int random) {
        byte[] bytes = new byte[random];
        Random ran = new Random();
        ran.nextBytes(bytes);
        return bytes;
    }

    /**
     * 生成随机数
     *
     * @return
     */
    public static byte[] getRandomByte1(int num) {
        byte[] bytes = new byte[num];
        Random r = new Random();
        for (int i = 0; i < num; i++) {
            int i1 = r.nextInt();
            bytes[i] = (byte) i1;
        }
        r = null;
        return bytes;
    }

    /**
     * 获取16进制的时间表示
     *
     * @return
     */
    public static byte[] getTimeHexString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "yyyyMMddHHmmss");
        // 20151007205012
        String time = simpleDateFormat.format(new Date(System.currentTimeMillis()));
        String s = time.substring(0, 2);
        if (s.length() % 2 == 1) {
            s = "0" + s;
        }
        String yearString2 = time.substring(2, 4);
        if (yearString2.length() % 2 == 1) {
            yearString2 = "0" + yearString2;
        }

        String month = time.substring(4, 6);
        if (month.length() % 2 == 1) {
            month = "0" + month;
        }
        String day = time.substring(6, 8);
        if (day.length() % 2 == 1) {
            day = "0" + day;
        }
        String hour = time
                .substring(8, 10);
        if (hour.length() % 2 == 1) {
            hour = "0" + hour;
        }
        String minute = time.substring(10,
                12);
        if (minute.length() % 2 == 1) {
            minute = "0" + minute;
        }
        String second = time.substring(12);
        if (second.length() % 2 == 1) {
            second = "0" + second;
        }
        String timString = s + yearString2 + month + day + hour + minute + second;

        return HexString2Bytes(timString);
    }

    /**
     * 16进制时间显示
     *
     * @param time  时间参数  但是是没有补0的
     * @return newTime 时分秒 补零的 不需要判断
     */
    public static byte[] timeHexString(String time,String newTime) {
        String[] split = time.split("-");
        if (split[1].length() % 2 == 1) {
            split[1] = "0" + split[1];
        }
        if (split[2].length() % 2 == 1) {
            split[2] = "0" + split[2];
        }
        time = split[0] + split[1] + split[2] + newTime;
        //20170508 185424
        String yearString1 = time.substring(0, 2);
        if (yearString1.length() % 2 == 1) {
            yearString1 = "0" + yearString1;
        }
        String yearString2 = time.substring(2, 4);
        if (yearString2.length() % 2 == 1) {
            yearString2 = "0" + yearString2;
        }

        String month = time.substring(4, 6);
        if (month.length() % 2 == 1) {
            month = "0" + month;
        }
        String day = time.substring(6, 8);
        if (day.length() % 2 == 1) {
            day = "0" + day;
        }
        String hour = time
                .substring(8, 10);
        if (hour.length() % 2 == 1) {
            hour = "0" + hour;
        }
        String minute = time.substring(10,
                12);
        if (minute.length() % 2 == 1) {
            minute = "0" + minute;
        }
        String second = time.substring(12);
        if (second.length() % 2 == 1) {
            second = "0" + second;
        }
        String timString = yearString1 + yearString2 + month + day + hour + minute + second;
        return HexString2Bytes(timString);
    }

    /**
     * @param strPart 字符串
     * @return 16进制字符串
     * @throws
     * @Title:string2HexString
     * @Description:字符串转16进制字符串
     */
    public static String string2HexString(String strPart) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < strPart.length(); i++) {
            int ch = (int) strPart.charAt(i);
            String strHex = Integer.toHexString(ch);
            hexString.append(strHex);
        }
        return hexString.toString();
    }

    /**
     *分包适配
     * @param allBytes 需要分包的总byte
     * @param fenbaoNum 需要分几个包
     * @param zhiling 指令
     * @param length 数据体长度
     * @return
     */
    public static List<byte[]> fenBao(byte[] allBytes, int fenbaoNum, int zhiling, int length) {
        /**
         * 分包思路 假设这个byte总数组长度为81 ，那么我们一般分为5个包发送 最后一包不足补零
         * 其中可变的是分包号.第一包的指令和长度,可变的通过用户传入
         * 1.用户需用确定自己需要分几个包
         * 2.确认分几个包之后，就是第一个包需要分包号，指令 和长度
         */

        List<byte[]> mDatas = new ArrayList<>();
        switch (fenbaoNum) {
            case 2:
                byte[] xiaoyan2 = new byte[40];
                xiaoyan2[0] = (byte) zhiling;//指令
                xiaoyan2[1] = 0x00;//长度1
                xiaoyan2[2] = (byte) length;//长度2
                System.arraycopy(allBytes, 0, xiaoyan2, 3, allBytes.length);
                //效验码
                int innerCRCValue2 = CRC16Util.calcCrc16(xiaoyan2);
                byte[] innerCRCByte2 = Util.hexString2Byte(Integer
                        .toHexString(innerCRCValue2));
                Logger.d("效验为：" + Util.Bytes2HexString(innerCRCByte2));
                System.arraycopy(innerCRCByte2, 0, xiaoyan2, allBytes.length + 2, innerCRCByte2.length);

                byte[] bytes1 = new byte[20];
                byte[] bytes2 = new byte[20];
                bytes1[0] = 0x01;
                bytes2[0] = (byte) 0xFE;
                System.arraycopy(xiaoyan2, 0, bytes1, 1, 19);
                System.arraycopy(xiaoyan2, 19, bytes2, 1, 19);
                mDatas.add(bytes1);
                mDatas.add(bytes2);
                break;
            case 3:
                byte[] xiaoyan_3 = new byte[60];
                xiaoyan_3[0] = (byte) zhiling;//指令
                xiaoyan_3[1] = 0x00;//长度1
                xiaoyan_3[2] = (byte) length;//长度2
                System.arraycopy(allBytes, 0, xiaoyan_3, 3, allBytes.length);
                //效验码
                int innerCRCValue = CRC16Util.calcCrc16(xiaoyan_3);
                byte[] innerCRCByte = Util.hexString2Byte(Integer
                        .toHexString(innerCRCValue));
                Logger.d("效验为：" + Util.Bytes2HexString(innerCRCByte));
                System.arraycopy(innerCRCByte, 0, xiaoyan_3, allBytes.length + 2, innerCRCByte.length);
                byte[] bytes1_3 = new byte[20];
                byte[] bytes2_3 = new byte[20];
                byte[] bytes3_3 = new byte[20];
                bytes1_3[0] = 0x01;
                bytes2_3[0] = 0x02;
                bytes3_3[0] = (byte) 0xFE;
                System.arraycopy(xiaoyan_3, 0, bytes1_3, 1, 19);
                System.arraycopy(xiaoyan_3, 19, bytes2_3, 1, 19);
                System.arraycopy(xiaoyan_3, 38, bytes3_3, 1, 19);
                mDatas.add(bytes1_3);
                mDatas.add(bytes2_3);
                mDatas.add(bytes3_3);
                break;
            case 4:
                byte[] xiaoyan_4 = new byte[80];
                xiaoyan_4[0] = (byte) zhiling;//指令
                xiaoyan_4[1] = 0x00;//长度1
                xiaoyan_4[2] = (byte) length;//长度2
                System.arraycopy(allBytes, 0, xiaoyan_4, 3, allBytes.length);
                //效验码
                int innerCRCValue_4 = CRC16Util.calcCrc16(xiaoyan_4);
                byte[] innerCRCByte_4 = Util.hexString2Byte(Integer
                        .toHexString(innerCRCValue_4));
                Logger.d("效验为：" + Util.Bytes2HexString(innerCRCByte_4));
                System.arraycopy(innerCRCByte_4, 0, xiaoyan_4, allBytes.length + 2, innerCRCByte_4.length);
                byte[] bytes1_4 = new byte[20];
                byte[] bytes2_4 = new byte[20];
                byte[] bytes3_4 = new byte[20];
                byte[] bytes4_4 = new byte[20];
                bytes1_4[0] = 0x01;
                bytes2_4[0] = 0x02;
                bytes3_4[0] = 0x03;
                bytes4_4[0] = (byte) 0xFE;
                System.arraycopy(xiaoyan_4, 0, bytes1_4, 1, 19);
                System.arraycopy(xiaoyan_4, 19, bytes2_4, 1, 19);
                System.arraycopy(xiaoyan_4, 38, bytes3_4, 1, 19);
                System.arraycopy(xiaoyan_4, 57, bytes4_4, 1, 19);
                mDatas.add(bytes1_4);
                mDatas.add(bytes2_4);
                mDatas.add(bytes3_4);
                mDatas.add(bytes4_4);
                break;
            case 5:
                byte[] xiaoyan_5 = new byte[100];
                xiaoyan_5[0] = (byte) zhiling;//指令
                xiaoyan_5[1] = 0x00;//长度1
                xiaoyan_5[2] = (byte) length;//长度2
                System.arraycopy(allBytes, 0, xiaoyan_5, 3, allBytes.length);
                //效验码
                int innerCRCValue_5 = CRC16Util.calcCrc16(xiaoyan_5);
                byte[] innerCRCByte_5 = Util.hexString2Byte(Integer
                        .toHexString(innerCRCValue_5));
                Logger.d("效验为：" + Util.Bytes2HexString(innerCRCByte_5));
                System.arraycopy(innerCRCByte_5, 0, xiaoyan_5, allBytes.length + 2, innerCRCByte_5.length);
                byte[] bytes1_5 = new byte[20];
                byte[] bytes2_5 = new byte[20];
                byte[] bytes3_5 = new byte[20];
                byte[] bytes4_5 = new byte[20];
                byte[] bytes5_5 = new byte[20];
                bytes1_5[0] = 0x01;
                bytes2_5[0] = 0x02;
                bytes3_5[0] = 0x03;
                bytes4_5[0] = 0x04;
                bytes5_5[0] = (byte) 0xFE;
                System.arraycopy(xiaoyan_5, 0, bytes1_5, 1, 19);
                System.arraycopy(xiaoyan_5, 19, bytes2_5, 1, 19);
                System.arraycopy(xiaoyan_5, 38, bytes3_5, 1, 19);
                System.arraycopy(xiaoyan_5, 57, bytes4_5, 1, 19);
                System.arraycopy(xiaoyan_5, 76, bytes5_5, 1, 19);
                mDatas.add(bytes1_5);
                mDatas.add(bytes2_5);
                mDatas.add(bytes3_5);
                mDatas.add(bytes4_5);
                mDatas.add(bytes5_5);
                break;
            case 6:
                byte[] xiaoyan_6 = new byte[120];
                xiaoyan_6[0] = (byte) zhiling;//指令
                xiaoyan_6[1] = 0x00;//长度1
                xiaoyan_6[2] = (byte) length;//长度2
                System.arraycopy(allBytes, 0, xiaoyan_6, 3, allBytes.length);
                //效验码
                int innerCRCValue_6 = CRC16Util.calcCrc16(xiaoyan_6);
                byte[] innerCRCByte_6 = Util.hexString2Byte(Integer
                        .toHexString(innerCRCValue_6));
                Logger.d("效验为：" + Util.Bytes2HexString(innerCRCByte_6));
                System.arraycopy(innerCRCByte_6, 0, xiaoyan_6, allBytes.length + 2, innerCRCByte_6.length);
                byte[] bytes1_6 = new byte[20];
                byte[] bytes2_6 = new byte[20];
                byte[] bytes3_6 = new byte[20];
                byte[] bytes4_6 = new byte[20];
                byte[] bytes5_6 = new byte[20];
                byte[] bytes6_6 = new byte[20];
                bytes1_6[0] = 0x01;
                bytes2_6[0] = 0x02;
                bytes3_6[0] = 0x03;
                bytes4_6[0] = 0x04;
                bytes5_6[0] = 0x05;
                bytes6_6[0] = (byte) 0xFE;
                System.arraycopy(xiaoyan_6, 0, bytes1_6, 1, 19);
                System.arraycopy(xiaoyan_6, 19, bytes2_6, 1, 19);
                System.arraycopy(xiaoyan_6, 38, bytes3_6, 1, 19);
                System.arraycopy(xiaoyan_6, 57, bytes4_6, 1, 19);
                System.arraycopy(xiaoyan_6, 76, bytes5_6, 1, 19);
                System.arraycopy(xiaoyan_6, 95, bytes6_6, 1, 19);
                mDatas.add(bytes1_6);
                mDatas.add(bytes2_6);
                mDatas.add(bytes3_6);
                mDatas.add(bytes4_6);
                mDatas.add(bytes5_6);
                mDatas.add(bytes6_6);
                break;
            default:
                break;
        }
        return mDatas;
    }

    /**
     * 字节转int
     *
     * @param buff
     * @return
     */
    public static int ByteToInt(byte buff) {
        byte[] a = { 0x00, 0x00, 0x00, 0x00 };
        a[3] = buff;
        return ByteToInt(a, false);
    }

    /**
     * 字节数组转换成整数
     *
     * @param buf
     * @param asc
     *            false是16进制 trueunknown
     * @return
     */
    public final static int ByteToInt(byte[] buf, boolean asc) {
        if (buf == null) {
            throw new IllegalArgumentException("byte array is null!");
        }
        if (buf.length > 4) {
            throw new IllegalArgumentException("byte array size > 4 !");
        }
        int r = 0;
        if (asc)
            for (int i = buf.length - 1; i >= 0; i--) {
                r <<= 8;
                r |= (buf[i] & 0x000000ff);
            }
        else
            for (int i = 0; i < buf.length; i++) {
                r <<= 8;
                r |= (buf[i] & 0x000000ff);
            }
        return r;
    }
}
