package cn.tdchain.jbcc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Description：敏感词
 * @author xiaoming
 * 2019年4月18日
 */
public class SensitiveWordsUtil {
    public static String filePath = "sensitive-words.txt";//敏感词库文件路径
    public static Set<String> words;
    public static Map<String,String> wordMap;
    public static int MATCH_TYPE_MIN = 1;      //最小匹配规则
    public static int MATCH_TYPE_MAX = 2;      //最大匹配规则
    static{
        SensitiveWordsUtil.words = readTxtByLine(filePath);
        addSensitiveWords2Map(SensitiveWordsUtil.words);
    }
    public static Set<String> readTxtByLine(String filePath){
        Set<String> keyWordSet = new HashSet<String>();
        String realPath = SensitiveWordsUtil.class.getClassLoader().getResource(filePath).getFile();
        File file=new File(realPath);
        if(!file.exists()){      //文件流是否存在
            return keyWordSet;
        }
        BufferedReader reader=null;
        String temp=null;
        //int line=1;
        try{
            //reader=new BufferedReader(new FileReader(file));这样在web运行的时候，读取会乱码
            reader=new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
            while((temp=reader.readLine())!=null){
                //System.out.println("line"+line+":"+temp);
                keyWordSet.add(temp);
                //line++;
            }
        } catch(Exception e){
            e.printStackTrace();
        } finally{
            if(reader!=null){
                try{
                    reader.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        return keyWordSet;
    }
    /**
     * Description：检查文字中是否包含敏感字符，检查规则如下：<br>
     * @param txt
     * @param beginIndex
     * @param matchType
     * @return，如果存在，则返回敏感词字符的长度，不存在返回0
     * @version 1.0
     */
    @SuppressWarnings({ "rawtypes"})
    public static int checkSensitiveWords(String txt, int beginIndex, int matchType){
        boolean  flag = false;    //敏感词结束标识位：用于敏感词只有1位的情况
        int matchFlag = 0;     //匹配标识数默认为0
        char word = 0;
        Map nowMap = wordMap;
        for(int i = beginIndex; i < txt.length() ; i++){
            word = txt.charAt(i);
            nowMap = (Map) nowMap.get(word);     //获取指定key
            if(nowMap != null){     //存在，则判断是否为最后一个
                matchFlag++;     //找到相应key，匹配标识+1
                if("1".equals(nowMap.get("isEnd"))){       //如果为最后一个匹配规则,结束循环，返回匹配标识数
                    flag = true;       //结束标志位为true
                    if(MATCH_TYPE_MIN == matchType){    //最小规则，直接返回,最大规则还需继续查找
                        break;
                    }
                }
            }
            else{     //不存在，直接返回
                break;
            }
        }
        /*“粉饰”匹配词库：“粉饰太平”竟然说是敏感词
         * “个人”匹配词库：“个人崇拜”竟然说是敏感词
         * if(matchFlag < 2 && !flag){
            matchFlag = 0;
        }*/
        if(!flag){
            matchFlag = 0;
        }
        return matchFlag;
    }

    /**
     * Description：判断文字是否包含敏感字符
     * @param txt  文字
     * @param matchType  匹配规则 1：最小匹配规则，2：最大匹配规则
     * @return 若包含返回true，否则返回false
     * @version 1.0
     */
    public static boolean containsSensitiveWords(String txt, int matchType){
        boolean flag = false;
        for(int i = 0 ; i < txt.length() ; i++){
            int matchFlag = checkSensitiveWords(txt, i, matchType); //判断是否包含敏感字符
            if(matchFlag > 0){    //大于0存在，返回true
                flag = true;
            }
        }
        return flag;
    }

    /**
     *Description： 替换敏感字字符
     * @param txt
     * @param matchType
     * @param replaceChar 替换字符，默认*
     * @version 1.0
     */
    public static String replaceSensitiveWords(String txt, int matchType, String replaceChar){
        String resultTxt = txt;
        Set<String> set = getSensitiveWords(txt, matchType);     //获取所有的敏感词
        Iterator<String> iterator = set.iterator();
        String word = null;
        String replaceString = null;
        while (iterator.hasNext()) {
            word = iterator.next();
            replaceString = getReplaceChars(replaceChar, word.length());
            resultTxt = resultTxt.replaceAll(word, replaceString);
        }

        return resultTxt;
    }
    /**
     * Description：获取文字中的敏感词
     * @param txt 文字
     * @param matchType 匹配规则 1：最小匹配规则，2：最大匹配规则
     * @return Set<String>
     * @version 1.0
     */
    public static Set<String> getSensitiveWords(String txt , int matchType){
        Set<String> sensitiveWordList = new HashSet<String>();
        for(int i = 0 ; i < txt.length() ; i++){
            int length = checkSensitiveWords(txt, i, matchType);    //判断是否包含敏感字符
            if(length > 0){    //存在,加入list中
                sensitiveWordList.add(txt.substring(i, i+length));
                i = i + length - 1;    //减1的原因，是因为for会自增
            }
        }

        return sensitiveWordList;
    }

    /**
     * Description：获取替换字符串
     * @param replaceChar
     * @param length
     * @return String
     * @version 1.0
     */
    private static String getReplaceChars(String replaceChar,int length){
        String resultReplace = replaceChar;
        for(int i = 1 ; i < length ; i++){
            resultReplace += replaceChar;
        }

        return resultReplace;
    }

    /**
     * Description：将我们的敏感词库构建成了一个类似与一颗一颗的树，这样我们判断一个词是否为敏感词时就大大减少了检索的匹配范围。
     * @param keyWordSet 敏感词库
     * @author yqwang0907
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void addSensitiveWords2Map(Set<String> keyWordSet) {
        wordMap = new HashMap(keyWordSet.size());     //初始化敏感词容器，减少扩容操作
        String key = null;
        Map nowMap = null;
        Map<String, String> newWorMap = null;
        //迭代keyWordSet
        Iterator<String> iterator = keyWordSet.iterator();
        while(iterator.hasNext()){
            key = iterator.next();    //关键字
            nowMap = wordMap;
            for(int i = 0 ; i < key.length() ; i++){
                char keyChar = key.charAt(i);       //转换成char型
                Object wordMap = nowMap.get(keyChar);       //获取

                if(wordMap != null){        //如果存在该key，直接赋值
                    nowMap = (Map) wordMap;
                }
                else{     //不存在则，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
                    newWorMap = new HashMap<String,String>();
                    newWorMap.put("isEnd", "0");     //不是最后一个
                    nowMap.put(keyChar, newWorMap);
                    nowMap = newWorMap;
                }

                if(i == key.length() - 1){
                    nowMap.put("isEnd", "1");    //最后一个
                }
            }
        }
    }
}
