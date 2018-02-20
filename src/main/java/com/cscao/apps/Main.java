package com.cscao.apps;

import com.cscao.libs.mucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static void printUsage() {
        String usage = "index [INDEX_PATH] [DOCS_PATH] [-update]\n"
                + "\tThis indexes the documents in DOCS_PATH, creating a Lucene index\n"
                + "\tin INDEX_PATH that can be searched with: \n"
                + "search [INDEX_PATH] [QUERY] [TOP_N] [SIMILARITY_FUNCTION]\n"
                + "\tThis searches the QUERY in INDEX_PATH, using a SIMILARITY_FUNCTION ('lm', i.e., " +
                "LMDirichletSimilarity by default) function, and return TOP_N documents (5 by default) \n";
        System.err.println(usage);
        System.exit(-1);
    }

    public static void main(String[] args) throws IOException, ParseException {

        if (args.length < 1) {
            printUsage();
        } else {
            String func = args[0];
            if (func.toLowerCase().startsWith("index")) {
                doIndexing(args);
            } else if (func.toLowerCase().startsWith("search")) {
                doSearching(args);
            } else {
                System.err.println("invalid args:" + func + "!");
                printUsage();
            }
        }


    }

    private static void doIndexing(String[] args) {
        Map<String, String> funcArgs = new HashMap<>();
        if (args.length < 3) {
            printUsage();
        } else {
            String docPath = args[1];
            String indexPath = args[2];
            if (args.length > 3) {
                if (args[3].toLowerCase().startsWith("-u")) {
                    funcArgs.put("update", "true");
                } else {
                    System.err.println("unrecognized arg: " + args[3]);
                    printUsage();
                }
            } else{
                funcArgs.put("update", "false");
            }
            funcArgs.put("docPath", docPath);
            funcArgs.put("indexPath", indexPath);
        }


        IndexTREC.buildIndex(funcArgs);
    }

    private static void doSearching(String[] args) throws IOException, ParseException {

        Map<String, String> funcArgs = new HashMap<>();
        if (args.length < 3) {
            printUsage();
        } else {
            String indexPath = args[1];
            String query = args[2];
            funcArgs.put("indexPath", indexPath);
            funcArgs.put("question", query);

            String topN = "5";
            String simFunc = "lm";
            if (args.length >= 4) {
                try {
                    int n = Integer.parseInt(args[3]);
                    if (n > 0 && n < 1000) {
                        topN = args[3];
                    } else {
                        System.err.println("topN:"+n+" should be larger than 0 and less than 1000");
                        printUsage();
                    }
                } catch (NumberFormatException e) {
                    System.err.println("topN should be an integer: " + args[3]);
                    printUsage();
                }

                if (args.length > 4){
                    simFunc = args[4];
                    if ("lm".equals(simFunc) || "dfr".equals(simFunc) || "bm25".equals(simFunc)) {
                        funcArgs.put("simFunc", simFunc);
                    } else {
                        System.err.println("unsupported similarity function: " + simFunc);
                        printUsage();
                    }
                }
            }
            funcArgs.put("topN", topN);
            funcArgs.put("simFunc", simFunc);
        }
        Search.searching(funcArgs);
    }
}
