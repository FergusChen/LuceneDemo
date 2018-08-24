package cc.ferguschen.retrieval.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by chenqining on 17/8/29.
 */
public class FileUtil {
    public enum State{
        SUCCESS,
        FILE_NOT_EXIST,
        NO_DIR_AUTHORITY,
    }

    /**
     * 加载资源文件的文件流
     * @param path 资源文件的文件名
     * @return
     */
    public static InputStream loadResource(String path){
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null){
            classLoader = FileUtil.class.getClassLoader();
        }
        try {
            InputStream in = classLoader.getResourceAsStream(path);
            return in;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将文本文件的数据读入词典中(文本文件是两列数据, 中间以\t隔开)
     * @param inFilepath  输入文件路径
     * @return 读取的词典
     */
    public static HashMap<String, String> loadMapFromFile(String inFilepath){
        HashMap<String, String> result = new HashMap<>();
        File inFile = new File(inFilepath);
        if (!inFile.exists()){
            System.out.println("input file is not exists: " + inFilepath);
            return result;
        }
        Long fileLength = inFile.length();
        byte[] content = new byte[fileLength.intValue()];
        try{
            FileInputStream in = new FileInputStream(inFile);
            in.read(content);
            in.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        String text = new String(content);
        String[] pairs = text.split("\n");
        for (String pair: pairs){
            String[] items = pair.split("\t");
            if (items.length == 2){
                result.put(items[0], items[1]);
            }
        }
        return result;
    }

    /**
     * key \t value 形式的文件, 加载并返回HashMap
     * @param inStream
     * @return
     */
    public static HashMap<String, String> loadMapFromFile(InputStream inStream){
        HashMap<String, String> result = new HashMap<>();
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
            String line;
            while((line = reader.readLine()) != null){
                String[] items = line.split("\t");
                if (items.length == 2){
                    result.put(items[0], items[1]);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                if (reader != null){
                    reader.close();
                }
                inStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 将文本文件的每一行读到ArrayList中
     * @param inFilepath
     * @return
     */
    public static ArrayList<String> loadListFromFile(String inFilepath){
        ArrayList<String> result = new ArrayList<>();
        File inFile = new File(inFilepath);
        if (!inFile.exists()){
            System.out.println("input file is not exists: " + inFilepath);
            return result;
        }
        try{
            BufferedReader reader = new BufferedReader(new FileReader(inFile));
            String line = null;
            while((line = reader.readLine()) != null){
                result.add(line);
            }
            reader.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return result;
    }

    public static HashSet<String> loadSetFromFile(InputStream inStream){
        HashSet<String> result = new HashSet<>();
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
            String line;
            while((line = reader.readLine()) != null){
                result.add(line);
            }
            reader.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return result;
    }
    /**
     * 将文本文件的每一行读到ArrayList中
     * @param inFilepath
     * @return
     */
    public static HashSet<String> loadSetFromFile(String inFilepath){
        HashSet<String> result = new HashSet<>();
        File inFile = new File(inFilepath);
        if (!inFile.exists()){
            System.out.println("input file is not exists: " + inFilepath);
            return result;
        }
        try{
            BufferedReader reader = new BufferedReader(new FileReader(inFile));
            String line = null;
            while((line = reader.readLine()) != null){
                result.add(line);
            }
            reader.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 加载Json数组
     * @param fileStream  输入文件流
     * @return JSONArray对象, 包含转换的JSON对象
     */
    public static JSONArray loadJSONArray(InputStream fileStream){
        if (fileStream == null){
            System.out.println("can't load jsonArray, fileStream is null");
            return null;
        }
        JSONArray result = new JSONArray();
        BufferedReader reader;
        String line = "";
        try{
            reader = new BufferedReader(new InputStreamReader(fileStream));

            while((line = reader.readLine()) != null){
                JSONObject curObj = JSON.parseObject(line);
                result.add(curObj);
            }
            reader.close();
        }catch (IOException e){
            e.printStackTrace();
        }catch (JSONException e){
            System.out.println("file content has illegal format: " + line);
            e.printStackTrace();
            return null;
        }
        return result;
    }

    /**
     * 根据输入文件路径和输出文件路径, 1. 判断输入路径是否存在, 2. 创建输出文件路径的父目录.
     * @param inFilepath
     * @param outFilepath
     * @return
     */
    public static State prepareDir(String inFilepath, String outFilepath){
        File inFile = new File(inFilepath);
        File outFile = new File(outFilepath);
        File outDir;
        if (outFile.isDirectory()){
            outDir = outFile;
        }else{
            outDir = new File(outFile.getParent());
        }
        if (!outDir.exists()){
            boolean dirResult = outDir.mkdirs();
            if (!dirResult){
                return State.NO_DIR_AUTHORITY;
            }
        }
        if (inFile.exists()){
            return State.SUCCESS;
        }else{
            return State.FILE_NOT_EXIST;
        }
    }

    /**
     * 准备输出路径的父目录.
     * @param outFilepathArr  输出路径的可变地址列表
     * @return  状态
     */
    public static State prepareOutDir(String ...outFilepathArr){
        for(String outFilepath: outFilepathArr){
            File outFile = new File(outFilepath);
            String outParentDir = outFile.getParent();
            File outDir = new File(outParentDir);
            if (!outDir.exists()){
                boolean dirResult = outDir.mkdirs();
                if (!dirResult){
                    return State.NO_DIR_AUTHORITY;
                }
            }
        }
        return State.SUCCESS;
    }

    /**
     * 查看输入路径是否都存在.
     * @param inFilepathArr  输入路径的可变地址列表
     * @return  状态
     */
    public static State checkInFilepath(String ...inFilepathArr){
        for(String inFilepath: inFilepathArr){
            File inFile = new File(inFilepath);
            if (!inFile.exists()){
                return State.FILE_NOT_EXIST;
            }
        }
        return State.SUCCESS;
    }

    public static void closeReaderWriter(Reader reader, Writer writer){
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
