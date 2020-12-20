package de.mytb.liretools;

public class StdOutWriter implements Writer {
    @Override
    synchronized public void println(String text) {
        System.out.println(text);
    }


}
