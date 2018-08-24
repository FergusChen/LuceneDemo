package cc.ferguschen.retrieval;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BoostAttributeImpl;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by chenqining on 2018/7/17.
 * 索引的追加, 更新和删除
 */
public class IndexDemo {
    private String idxPath = "idx/data";   //存放索引的目录
    private Analyzer analyzer = null;
    private Directory idxDir = null;

    private boolean initIdx(){
        try {
            if (analyzer == null) {
                CharArraySet stopWordsSet = new CharArraySet(Arrays.asList("镇", "村", "市", "乡"), true);  //可以自定义停用词, 第2个参数控制忽略大小写.
                analyzer = new StandardAnalyzer(stopWordsSet);
                idxDir = FSDirectory.open(Paths.get(idxPath));
            }
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 索引追加
     * @param name
     * @param addr
     * @param id
     */
    private void appendIndex(String name, String addr, String id){
        if (analyzer == null && !initIdx()){
            System.out.println("[error] fail to init index");
            return;
        }
        try {
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.APPEND);
            IndexWriter iwriter = new IndexWriter(idxDir, config);

            Document doc = new Document();
            doc.add(new StringField("id", id, Field.Store.YES));
            doc.add(new TextField("name", name, Field.Store.YES));
            doc.add(new TextField("addr", addr, Field.Store.NO));
            iwriter.addDocument(doc);
            iwriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    /**
     * 更新指定id的name
     * @param name
     * @param id
     */
    private void updateIndex(String name, String id){
        if (analyzer == null && !initIdx()){
            System.out.println("[error] fail to init index");
            return;
        }
        try{
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.APPEND);  //默认是CREATE_OR_APPEND;
            IndexWriter iwriter = new IndexWriter(idxDir, config);

            //创建新的文档
            Document doc = new Document();
            doc.add(new StringField("id", id, Field.Store.YES));
            doc.add(new TextField("name", name, Field.Store.YES));
            doc.add(new TextField("address", "测试地址,保存", Field.Store.YES));
            Field f = new TextField("contents", "hello word", Field.Store.NO);
            iwriter.updateDocument(new Term("id", id), doc);  //lucene是用新文档替换掉符合条件的文档, 所以, 结果是以前的文档删除, 又追加新文档.
            iwriter.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }



    private void deleteIndex(String id){
        if (analyzer == null && !initIdx()){
            System.out.println("[error] fail to init index");
            return;
        }
        try{
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.APPEND);
            IndexWriter iwriter  = new IndexWriter(idxDir, config);
            //方法一: 通过Term删除
            System.out.println("delete Value:" +iwriter.deleteDocuments(new Term("id", id)));

            //方法二: 通过查询删除
//            QueryParser queryParser = new QueryParser("id", analyzer);
//            Query query = queryParser.parse(id);
//            iwriter.deleteDocuments(query);

            //上面两种删除的做法只是将document放入了回收站, 可以在同一个方法中 用rollback恢复.
            //iwriter.rollback();  // 同一方法中, 可以用rollback恢复已放到回收站的索引;

//            iwriter.forceMergeDeletes();   //强制删除已删除的文档.
            iwriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        IndexDemo demo = new IndexDemo();
        String appendID = "1234567";
        String name = "追加饭店";
        String addr = "上海市浦东新区学业路302号";
        demo.appendIndex(name, addr, appendID);

        //删除文档
        String delID = "1234567";
        demo.deleteIndex(delID);

        //更新文档
        String updateID = "22130905";
//        demo.updateIndex("测试修改的POI Name", updateID);

    }
}
