/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.bookxmlparser;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

/**
 *
 * @author Kasper
 */
public class Main {
    
    
     public static void main(String[] args){
         BookParser bp = new BookParser();
         try {
             bp.Parse();
         } catch (ParserConfigurationException ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SAXException ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         } catch (XPathExpressionException ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         }
         
    }
}
