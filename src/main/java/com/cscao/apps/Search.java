package com.cscao.apps;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple command-line based search demo.
 */
public class Search {

    private Search() {
    }

    private static Similarity getSimilarityFunction(String simFunc) {
        Similarity simfn;
        if ("bm25".equals(simFunc)) {
            simfn = new BM25Similarity();
        } else if ("dfr".equals(simFunc)) {
            simfn = new DFRSimilarity(new BasicModelP(), new AfterEffectL(), new NormalizationH2());
        } else {
            simfn = new LMDirichletSimilarity();
        }
        return simfn;
    }

    public static void searching(Map<String, String> args) throws IOException, ParseException {
        String indexPath = args.get("indexPath");
        String question = args.get("question");
        String simFunc = args.get("simFunc");
        String topN = args.get("topN");

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(getSimilarityFunction(simFunc));

        Analyzer analyzer = new StandardAnalyzer();
        QueryParser parser = new QueryParser("text", analyzer);
        Query query = parser.parse(question);

        doBatchSearch(searcher, query, Integer.parseInt(topN));
    }

    /**
     * Simple command-line based search demo.
     */
    public static void main(String[] args) throws Exception {
        String usage =
                "Usage:\tjava com.cscao.apps.BatchSearch [-index dir] [-simfn similarity] [-field f] [-queries file]";
        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);
            System.out.println("Supported similarity functions:\ndefault: DefaultSimilary (tfidf)\n");
            System.exit(0);
        }

        String index = "index";
        String field = "text";
        String queries = null;
        String simstring = "lm";

        for (int i = 0; i < args.length; i++) {
            if ("-index".equals(args[i])) {
                index = args[i + 1];
                i++;
            } else if ("-field".equals(args[i])) {
                field = args[i + 1];
                i++;
            } else if ("-queries".equals(args[i])) {
                queries = args[i + 1];
                i++;
            } else if ("-simfn".equals(args[i])) {
                simstring = args[i + 1];
                i++;
            }
        }

        Similarity simfn = getSimilarityFunction(simstring);
        if (simfn == null) {
            System.out.println(usage);
            System.out.println("Supported similarity functions:\ndefault: Language model, Dirichlet smoothing");
            System.out.println("bm25: BM25Similarity (standard parameters)");
            System.out.println("dfr: Divergence from Randomness model (PL2 variant)");
            System.exit(0);
        }

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(simfn);
        Analyzer analyzer = new StandardAnalyzer();

        BufferedReader in;
        if (queries != null) {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(queries), "UTF-8"));
        } else {
            in = new BufferedReader(new InputStreamReader(new FileInputStream("queries"), "UTF-8"));
        }
        QueryParser parser = new QueryParser(field, analyzer);
        while (true) {
            String line = in.readLine();

            if (line == null) {
                break;
            }

            line = line.trim();
            if (line.length() == 0) {
                break;
            }

            String[] pair = line.split(" ", 2);
            Query query = parser.parse(pair[1]);

            doBatchSearch(searcher, query, 10);
        }
        reader.close();
    }

    /**
     * This function performs a top-1000 search for the query as a basic TREC run.
     */
    public static void doBatchSearch(IndexSearcher searcher, Query query, int topN)
            throws IOException {

        // Collect enough docs to show 5 pages
        TopDocs results = searcher.search(query, topN);
        ScoreDoc[] hits = results.scoreDocs;
        HashMap<String, String> seen = new HashMap<>(topN);
        int numTotalHits = Math.toIntExact(results.totalHits);

        int start = 0;
        int end = Math.min(numTotalHits, topN);

        for (int i = start; i < end; i++) {
            Document doc = searcher.doc(hits[i].doc);
            String docId = doc.get("id");
            String docText = doc.get("text");
            // There are duplicate document numbers in the FR collection, so only output a given
            // docId once.
            if (seen.containsKey(docId)) {
                continue;
            }
            seen.put(docId, docId);
            System.out.println(docId + " " + hits[i].score + " |TEXT:===>| " +docText);
        }
    }
}

