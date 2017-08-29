package com.bbridge.test;

/**
 * Created by shambala on 27.08.17.
 */
public class Main {
    public static void main(String[] args) {
        new Downloader().authorize("mescase", "tO4klafo");
        new Downloader().getBbridgeResponse("1");
    }
}
