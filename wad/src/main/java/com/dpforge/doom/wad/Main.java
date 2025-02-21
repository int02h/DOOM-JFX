package com.dpforge.doom.wad;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, WadException, ParserConfigurationException, SAXException {
        File wadFile = new File("/Users/dpopov/Projects/personal/DOOM-JFX/doom-jfx/doom.wad");
        System.out.println(new WadFileReader().read(wadFile));
    }
}