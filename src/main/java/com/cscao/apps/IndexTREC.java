package com.cscao.apps;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class IndexTREC {

    private IndexTREC() {
    }

    private static int processedDocCount = 0;

    public static void buildIndex(Map<String, String> args) {
        String docPath = args.get("docPath");
        String indexPath = args.get("indexPath");
        String update = args.get("update");
        boolean create = !Boolean.getBoolean(update);
        final Path docDir = Paths.get(docPath);
        if (!Files.isReadable(docDir)) {
            System.out.println("Document directory '" + docDir.toAbsolutePath()
                    + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }
        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexPath + "'...");

            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            if (create) {
                // Create a new index in the directory, removing any
                // previously indexed documents:
                iwc.setOpenMode(OpenMode.CREATE);
            } else {
                // Add new documents to an existing index:
                iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            }

            // Optional: for better indexing performance, if you
            // are indexing many documents, increase the RAM
            // buffer.  But if you do this, increase the max heap
            // size to the JVM (eg add -Xmx512m or -Xmx1g):
            //
            iwc.setRAMBufferSizeMB(256.0);

            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDocs(writer, docDir);

            // NOTE: if you want to maximize search performance,
            // you can optionally call forceMerge here.  This can be
            // a terribly costly operation, so generally it's only
            // worth it when your index is relatively static (ie
            // you're done adding documents to it):
            //
            // writer.forceMerge(1);

            writer.close();

            Date end = new Date();
            long milliseconds = end.getTime() - start.getTime();
            int seconds = (int) (milliseconds / 1000) % 60;
            int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
            int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
            System.out.println(String.format("%d hour %d min, %d sec", hours, minutes, seconds));

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String indexPath = null;
        String docsPath = null;
        String create = "true";
        for (int i = 0; i < args.length; i++) {
            if ("-index".equals(args[i])) {
                indexPath = args[i + 1];
                i++;
            } else if ("-docs".equals(args[i])) {
                docsPath = args[i + 1];
                i++;
            } else if ("-update".equals(args[i])) {
                create = "false";
            } else {
                System.out.println("unknown arg, skipped: " + args[i]);
            }
        }

        if (docsPath == null) {
            String usage = "java com.cscao.apps.IndexTREC"
                    + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
                    + "This indexes the documents in DOCS_PATH, creating a Lucene index"
                    + "in INDEX_PATH that can be searched with com.cscao.apps.SearchFiles";
            System.err.println(usage);
            System.exit(1);
        }

        Map<String, String> funcArgs = new HashMap<>();
        funcArgs.put("indexPath", indexPath);
        funcArgs.put("docPath", docsPath);
        funcArgs.put("update", create);

        IndexTREC.buildIndex(funcArgs);

    }

    /**
     * Indexes the given file using the given writer, or if a directory is given,
     * recurses over files and directories found under the given directory.
     * <p>
     * NOTE: This method indexes one document per input file.  This is slow.  For good
     * throughput, put multiple documents into your input file(s).  An example of this is
     * in the benchmark module, which can create "line doc" files, one document per line,
     * using the
     * <a href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
     * >WriteLineDocTask</a>.
     *
     * @param writer Writer to the index where the given file/dir info will be stored
     * @param path   The file to index, or the directory to recurse into to find files to index
     * @throws IOException If there is a low-level I/O error
     */
    private static void indexDocs(IndexWriter writer, Path path)
            throws IOException {
        // do not try to index files that cannot be read
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        indexDoc(writer, file);
                    } catch (IOException ignore) {
                        // don't index files that can't be read.
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            indexDoc(writer, path);
        }
    }

    /**
     * Indexes a single document
     */
    private static void indexDoc(IndexWriter writer, Path path) throws IOException {
        System.out.print("Reading file " + path.toString());
        TRECDocIterator docs = new TRECDocIterator(path);
        Document doc;
        int singleFileCount = 0;
        while (docs.hasNext()) {
            doc = docs.next();
            if (doc == null) {
                continue;
            }
            if (doc.getField("text") != null) {
                writer.addDocument(doc);
                singleFileCount++;
            }
        }
        processedDocCount += singleFileCount;
        System.out.println(", processed \033[36m" + processedDocCount + "\033[0m documents in total");
    }

}


