/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.bookxmlparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Kasper
 */
public class BookParser {

    private PrintWriter writerBook;
    private PrintWriter writerAuthor;
    private PrintWriter writerCrossTable;
    private ArrayList<Author> authors;
    private HashSet<String> authorNames;
    private HashSet<Integer> bookIDs;

    /*   private int bookID = 0;*/
    private int authorID = 1;
    private int status = 1;

    public BookParser() {
        try {
            writerBook = new PrintWriter("Book.csv", "UTF-8");
            writerBook.println("id,title");
            writerBook.flush();
            writerAuthor = new PrintWriter("Author.csv", "UTF-8");
            writerAuthor.println("id,name");
            writerAuthor.flush();
            writerCrossTable = new PrintWriter("Book_Author.csv", "UTF-8");
            writerCrossTable.println("book_id,author_id");
            writerCrossTable.flush();
            authors = new ArrayList<>();
            authorNames = new HashSet<>();
            bookIDs = new HashSet<>();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BookParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(BookParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Parse() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        //bookdir
        File bookdir = new File("D:\\Development\\Database\\Books\\books\\zipfiles");
        File[] directories = bookdir.listFiles();
        if (directories != null) {
            for (File child : directories) {
                String id = child.getName().replace(".txt", "");
                if (id.matches("^[0-9]*$")) {
                    bookIDs.add(Integer.parseInt(id));
                }
            }
        }

        //rdfdir
        File dir = new File("D:\\Development\\Database\\Books\\cache\\epub");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                convertRDF(child.listFiles()[0], child.getName());
                System.out.println("Status: " + status++ + " out of " + directoryListing.length);
            }
        } else {
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
        }
    }

    private void convertRDF(File f, String bookID) throws FileNotFoundException {
        if (bookIDs.contains(Integer.parseInt(bookID))) {
            ArrayList<Author> tempAuthors = new ArrayList();
            //Authors
            boolean authorOnbook = true;
            //       Property name = model.getProperty("http://www.gutenberg.org/2009/pgterms/name");
            //       ResIterator iterNames = model.listSubjectsWithProperty(name);*/
            ArrayList<String> creators = getCreatorNames(f);

            if (creators.size() > 0) {
                for (String name : creators) {
                    //add returns true if auther dosent exsist allready, because HashSet can only have Unique values
                    if (authorNames.add(name)) {
                        authors.add(new Author(authorID, name));
                        tempAuthors.add(new Author(authorID, name));
                        writerAuthor.println('"' + Integer.toString(authorID++) + '"' + "," + '"' + name.replaceAll("\"", "\"\"") + '"');
                        writerAuthor.flush();
                    } else {
                        for (Author a : authors) {
                            if (a.name.equals(name)) {
                                tempAuthors.add(a);
                            }
                        }
                    }
                }
            } else {
                System.out.println("Names not found");
                authorOnbook = false;
            }

            String data = "";
            //title What about books with no title?
            Model model = ModelFactory.createDefaultModel();
            model.read(new FileInputStream(f), null);
            Property title = model.getProperty("http://purl.org/dc/terms/title");
            ResIterator iterTitle = model.listSubjectsWithProperty(title);
            if (iterTitle.hasNext()) {
                while (iterTitle.hasNext()) {
                    data += iterTitle.nextResource()
                            .getProperty(title)
                            .getString() + " ";
                }

            } else {
                System.out.println("Title not found");
                data = "Unknown Book Title;";
            }

            Book book = new Book(bookID, data.substring(0, data.length() - 1));
            writerBook.println('"' + book.ID + '"' + "," + '"' + book.title.replaceAll("[\r\n]+", " ").replaceAll("\"", "\"\"") + '"');
            writerBook.flush();
            if (authorOnbook) {
                for (Author temp : tempAuthors) {
                    writerCrossTable.println('"' + book.ID + '"' + "," + '"' + Integer.toString(temp.Id) + '"');
                    writerCrossTable.flush();
                }
            }
        }
    }

    public ArrayList<String> getCreatorNames(File f) {
        //Want to read all book names from XML
        ArrayList<String> creatorNames = new ArrayList<String>();

        //Parse XML file
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new FileInputStream(f));

            //Get XPath expression
            XPathFactory xpathfactory = XPathFactory.newInstance();
            XPath xpath = xpathfactory.newXPath();
            XPathExpression expr = xpath.compile("//creator/agent/name/text()");

            //Search XPath expression
            Object result = expr.evaluate(doc, XPathConstants.NODESET);

            //Iterate over results and fetch book names
            NodeList nodes = (NodeList) result;
            for (int i = 0; i < nodes.getLength(); i++) {
                creatorNames.add(nodes.item(i).getNodeValue());
            }
        } catch (Exception ex) {

        }

        //Verify book names
        return creatorNames;
    }

}
