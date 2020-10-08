//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.lang.model.element.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.IOUtils;

public final class SpanishAnalyzer2 extends StopwordAnalyzerBase {
    private final CharArraySet stemExclusionSet;
    public static final String DEFAULT_STOPWORD_FILE = "spanish_stop.txt";

    public static CharArraySet getDefaultStopSet() {
        return SpanishAnalyzer2.DefaultSetHolder.DEFAULT_STOP_SET;
    }

    public SpanishAnalyzer2() {
        this(SpanishAnalyzer2.DefaultSetHolder.DEFAULT_STOP_SET);
    }

    public SpanishAnalyzer2(CharArraySet stopwords) {
        this(stopwords, CharArraySet.EMPTY_SET);
    }

    public SpanishAnalyzer2(CharArraySet stopwords, CharArraySet stemExclusionSet) {
        super(stopwords);
        this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(stemExclusionSet));
    }

    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new StandardTokenizer();
        TokenStream result = new LowerCaseFilter(source);
        TokenStream result = new StopFilter(result, this.stopwords);
        if (!this.stemExclusionSet.isEmpty()) {
            result = new SetKeywordMarkerFilter((TokenStream)result, this.stemExclusionSet);
        }
        SnowballFilter sn= new SnowballFilter(Spanish);

        //TokenStream result = new SpanishLightStemFilter((TokenStream)result);
        return new TokenStreamComponents(source, sn);
    }

    protected TokenStream normalize(String fieldName, TokenStream in) {
        return new LowerCaseFilter(in);
    }

    private static class DefaultSetHolder {
        static final CharArraySet DEFAULT_STOP_SET;

        private DefaultSetHolder() {
        }

        static {
            try {
                DEFAULT_STOP_SET = WordlistLoader.getSnowballWordSet(IOUtils.getDecodingReader(SnowballFilter.class, "spanish_stop.txt", StandardCharsets.UTF_8));
            } catch (IOException var1) {
                throw new RuntimeException("Unable to load default stopword set");
            }
        }
    }
}
