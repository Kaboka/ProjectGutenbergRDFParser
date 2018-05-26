/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.bookxmlparser;

/**
 *
 * @author Kasper
 */
public class Book {
    public String title;
    public String ID;

    Book(String bookID, String data) {
        ID = bookID;
        title = data;
    }
    
}
