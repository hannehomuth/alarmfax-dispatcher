/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.feuerwehr.kremmen.dispatcher.ocr;

import java.io.IOException;

/**
 *
 * @author jhomuth
 */
public class OCRException extends Exception {

    /**
     * Creates a new instance of <code>OCRException</code> without detail
     * message.
     */
    public OCRException() {
    }

    /**
     * Constructs an instance of <code>OCRException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public OCRException(String msg) {
        super(msg);
    }

    public OCRException(String string, IOException ex) {
        super(string, ex);

    }
}
