package com.bbridge.test;

import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by shambala on 27.08.17.
 */
public class AuthServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Downloader downloader = new Downloader();
        JSONObject authParams = new JSONObject(downloader.readFromStream(req.getInputStream()));
        downloader.authorize(authParams.getString("username"), authParams.getString("password"));
    }
}
