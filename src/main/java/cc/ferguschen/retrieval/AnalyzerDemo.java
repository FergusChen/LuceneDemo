package cc.ferguschen.retrieval;

import cc.ferguschen.retrieval.ik.AdvIKAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.StringReader;

/**
 * Created by chenqining on 2018/7/16.
 * 文本分析模块是Lucene非常重要的模块. 分析器用于对文本进行分词, 小写转换, 词性还原(将动词的过去式,进行时还原成动词动词原形), 停用词过滤等处理, 是后续建立索引的基础.
 * lucene支持多种分析器, 简单说明如下:
 * * StandardAnalyzer: 常用于英文分词, 并对单词进行词性还原和小写转换等,能够有效处理, 但中文用StandardAnalyzer只是按字分割.
 * * IKAnalyzer:结合词典分词和文法分析算法的中文分词技术
 * * WhitespaceAnalyzer: 仅仅按照空格进行分词的分词器
 * * SimpleAnalyzer:将数字停用 中文不起作用,只按照标点符号分割
 * * StopAnalyzer:将数字停用 中文不起作用,只按照标点符号分割
 * * CJKAnalyzer:二分法分词器,前后两个字两两结合进行分词,或产生太多的词,有很多冗余
 * * KeywordAnalyzer: 不进行分割
 */
public class AnalyzerDemo {

    private static void printAnalyzerResult(Analyzer analyzer, String sentence){

        StringReader reader = new StringReader(sentence);
        try{
            TokenStream tokenStream = analyzer.tokenStream("", reader);  //对reader进行分词操作, TokenStream是分词器处理之后的流,存储分词器的所有信息
            tokenStream.reset();  //重置流状态, 以便调用incrementToken获取token, 该方法使tokenStream可以复用.
            System.out.println("分析器:" + analyzer.getClass());
            //获取分词结果, CharTermAttribute是Token的文本
            CharTermAttribute term = tokenStream.getAttribute(CharTermAttribute.class);
            while(tokenStream.incrementToken()){  //通过incrementToken获取下一个token, 相当于迭代器的next和hasNext结合
                System.out.print(term.toString() + "|");
            }
            System.out.println("\n");
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public static void main(String[] args){
        String sentence = "I have a lot of dreams. 北京市海淀区. 河西路六巷,西羌大道南段,河北昌黎县昌黎大厦南边的红绿灯路口. 路14,东门路_平湖街道龙田镇龙兴街,长通家园I区云趣园一区8号楼 中国农业银行24小时自助银行(山大路) 北京798 天津路,海德花园南明大道北,S302,上虞区北街南明村X230";

        Analyzer analyzer =  new StandardAnalyzer();
        printAnalyzerResult(analyzer, sentence);

        analyzer = new WhitespaceAnalyzer();
        printAnalyzerResult(analyzer, sentence);

        analyzer = new CJKAnalyzer();
        printAnalyzerResult(analyzer, sentence);

        analyzer = new KeywordAnalyzer();
        printAnalyzerResult(analyzer, sentence);

        analyzer = new StopAnalyzer();
        printAnalyzerResult(analyzer, sentence);

        analyzer = new SimpleAnalyzer();
        printAnalyzerResult(analyzer, sentence);

        analyzer = new SmartChineseAnalyzer();
        printAnalyzerResult(analyzer, sentence);

        analyzer = new KeywordAnalyzer();
        printAnalyzerResult(analyzer, sentence);

        analyzer = new AdvIKAnalyzer();   //IK分词器, 扩展到高版本Lucene, 配置文件在 resources/
        printAnalyzerResult(analyzer, sentence);
    }
}
