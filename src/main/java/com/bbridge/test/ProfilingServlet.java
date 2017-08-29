package com.bbridge.test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by shambala on 27.08.17.
 */
public class ProfilingServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String screenName = req.getParameter("screen_name");
        String token = req.getHeader("Authorization");
        if (token == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            Writer writer = resp.getWriter();
            writer.write(new Downloader(token).getFollowersProfiling(screenName).toString());
        }
    }
}
