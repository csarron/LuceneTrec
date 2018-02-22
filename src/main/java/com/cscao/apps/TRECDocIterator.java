package com.cscao.apps;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class TRECDocIterator implements Iterator<Document> {

    private BufferedReader reader;
    private boolean isEndOfFile = false;

    TRECDocIterator(Path path) throws IOException {
        InputStream stream = Files.newInputStream(path);
        reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    @Override
    public boolean hasNext() {
        return !isEndOfFile;
    }

    private String waitFor(String tag) throws IOException {
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.contains(tag)) {
                return line;
            }
        }
        isEndOfFile = true;
        reader.close();
        return null;
    }

    @Override
    public Document next() {
        Document doc = new Document();
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            if (null == waitFor("<DOC>")) {
                return null;
            }

            String docNoLine = waitFor("<DOCNO>");
            if (docNoLine == null) {
                return null;
            }
            int start = docNoLine.indexOf("<DOCNO>") + 7;
            int end = docNoLine.indexOf("</DOCNO>");
            String docNo = docNoLine.substring(start, end).trim();
//            System.out.println(docNo);
            doc.add(new StringField("id", docNo, Field.Store.YES));

            String docTitleLine = waitFor("<TITLE>");
            if (docTitleLine == null) {
                return null;
            }
//            System.out.println(docTitleLine);
            start = docTitleLine.indexOf("<TITLE>") + 7;
            end = docTitleLine.indexOf("</TITLE>");
            String docTitle = docTitleLine.substring(start, end).trim();
//            System.out.println(docTitle);
            doc.add(new StringField("title", docTitle, Field.Store.YES));

            String textFirstLine = waitFor("<TEXT>");
            if (textFirstLine == null) {
                return null;
            } else {
                int startText = docNoLine.indexOf("<TEXT>") + 7;
                sb.append(textFirstLine.substring(startText));
            }

            while (true) {
                line = reader.readLine();
                if (line == null) {
                    isEndOfFile = true;
                    reader.close();
                    break;
                }

                if (line.startsWith("</TEXT>") || line.endsWith("</TEXT>")) {
                    break;
                }
                sb.append(line);
            }
            if (sb.length() > 50) { // skip short paragraphs
                doc.add(new TextField("text", sb.toString(), Field.Store.YES));
            } else {
                return null;
            }

        } catch (IOException e) {
            doc = null;
        }
        return doc;
    }

    @Override
    public void remove() {
        // Do nothing, but don't complain
    }

}
