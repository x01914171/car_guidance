package com.example.guidance;

public class StringUtils {
    
    /**
     * 检查字符串是否为空
     * @param str 要检查的字符串
     * @return 如果为空或null返回true
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 检查字符串是否不为空
     * @param str 要检查的字符串
     * @return 如果不为空返回true
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * 安全的字符串比较
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 如果相等返回true
     */
    public static boolean equals(String str1, String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        return str1.equals(str2);
    }
    
    /**
     * 字符串首字母大写
     * @param str 要处理的字符串
     * @return 首字母大写的字符串
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}