package cc.ferguschen.retrieval;

import cc.ferguschen.retrieval.utils.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by chenqining on 2018/7/16.
 * Lucene基本检索Demo, 包括索引写入 和 关键字检索.
 */
public class RetrievalDemo {
    private String idxPath = "idx/data";   //存放索引的目录
    private Analyzer analyzer = null;
    private Directory idxDir = null;


    /**
     * 初始化必要的组件
     * @return
     */
    private boolean initIdx() {
        try {
            if (analyzer == null) {
                CharArraySet stopWordsSet = new CharArraySet(Arrays.asList("镇", "村", "市", "乡"), true);  //可以自定义停用词, 第2个参数控制忽略大小写.
                analyzer = new StandardAnalyzer(stopWordsSet);
                idxDir = FSDirectory.open(Paths.get(idxPath));
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }



    /**
     * 创建索引, 并存储到指定目录
     * @param dataFilename 源数据文件目录
     */
    private void makeIndex(String dataFilename){
        if (analyzer == null && !initIdx()){
            System.out.println("[error] fail to init index");
            return;
        }
        InputStream dataStream = FileUtil.loadResource(dataFilename);
        assert dataStream != null;
        JSONArray dataList = FileUtil.loadJSONArray(dataStream);
        assert dataList != null;
        try{
            //创建索引写入器
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);  // 创建写入模式, 每一次都重新创建
            IndexWriter iwriter = new IndexWriter(idxDir, config);

            BM25Similarity si = new BM25Similarity();

            //将文档内容写入索引文件
            for(int i = 0; i < dataList.size(); i++){
                Document doc = new Document();

                JSONObject curAddr = dataList.getJSONObject(i);
                String addr = curAddr.getString("address");
                String shopName = curAddr.getString("name");
                String id = curAddr.getString("id");
                doc.add(new StringField("id", id, Field.Store.YES));   // 不分析, 直接存储字符串.
                doc.add(new TextField("name", shopName, Field.Store.YES));   // 分析(lucene分析器)并存储.
                doc.add(new TextField("addr", addr, Field.Store.NO));  //只分析, 不存储. 不存储的域, 在检索时无法获取其值.

                iwriter.addDocument(doc);
            }
            iwriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 用关键词进行检索
     * @param keyword 关键词
     */
    private void searchTest(String keyword){
        if (analyzer == null && !initIdx()){
            System.out.println("[error] fail to init index");
            return;
        }
        try {
            //1. 索引目录读取器, 打开索引目录
            DirectoryReader ireader = DirectoryReader.open(idxDir);
            //2. 根据索引目录, 创建索引器
            IndexSearcher isearcher = new IndexSearcher(ireader);
            //3. 解析查询
            QueryParser parser = new QueryParser("name", analyzer);  //传入analyzer分析关键字, 并指定要查询的域
            Query query = parser.parse(keyword);

            //4. 获取结果
            ScoreDoc[] hits = isearcher.search(query, 10).scoreDocs;  // 获取top10个查询结果.
            System.out.println("查询结果数: " + hits.length);
            for(int i = 0; i < hits.length; i++){
                Document  hitDoc = isearcher.doc(hits[i].doc);
                System.out.println("id:" + hitDoc.get("id") + "\tname:" + hitDoc.get("name") + "\taddr:" + hitDoc.get("addr")); //因为addr未存储,所以获取的都是null
            }
        }catch (IOException e){
            e.printStackTrace();
        }catch (ParseException e){
            e.printStackTrace();
            System.out.print("fail to parse Query");
        }
    }



    public static void main(String[] args){
        String keywords = "追加";
        RetrievalDemo demo1 =new RetrievalDemo();
//        demo1.makeIndex("shop.json");
        demo1.searchTest(keywords);


    }
}
