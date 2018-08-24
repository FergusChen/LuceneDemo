package cc.ferguschen.retrieval.ik;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.IOUtils;

import java.io.Reader;
import java.io.StringReader;

/**
 * Created by chenqining on 2018/2/24.
 */
public class AdvIKAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String arg0){
        Reader reader = null;
        try{
            reader = new StringReader(arg0);
            AdvIKTokenizer it = new AdvIKTokenizer(reader);
            return new TokenStreamComponents(it);
        }finally {
            IOUtils.closeWhileHandlingException(reader);
        }
    }
}
