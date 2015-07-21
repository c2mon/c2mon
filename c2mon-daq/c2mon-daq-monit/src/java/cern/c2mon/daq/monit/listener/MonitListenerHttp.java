
package cern.c2mon.daq.monit.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.Enumeration;
import java.util.Queue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.monit.MonitEventProcessor;
import cern.c2mon.daq.monit.MonitUpdateEvent;

/**
 * 
 * @author mbuttner
 */
@SuppressWarnings("serial")
public class MonitListenerHttp extends HttpServlet implements MonitListenerIntf {

    private static final Logger LOG = LoggerFactory.getLogger(MonitListenerHttp.class);
    
    private Queue<MonitUpdateEvent> eventQueue;    // reference to the @see EventProcessor queue
    
    private Server webServer;
    private int port;
    
    //
    // --- PUBLIC METHODS -----------------------------------------------------------------------
    //
    public void setPort(int port) {
        this.port = port;
    }
    
    public int getPort() {
        return this.port;
    }
    
    //
    // --- Implements AbstractHandler ----------------------------------------------------------
    //
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>Hello World</h1>");
        response.getWriter().println("session=" + request.getSession(true).getId());
        LOG.info("GET target: {}", request.getPathInfo());
        printParams(request);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>Hello World</h1>");
        response.getWriter().println("session=" + request.getSession(true).getId());
        
        InputStreamReader reader = new InputStreamReader(request.getInputStream());
        char[] cbuf = new char[1024];
        StringBuffer buf = new StringBuffer();
        while (reader.read(cbuf) != -1) {
            String s = new String(cbuf);
            buf.append(s);
        }
        LOG.info(">" + buf.toString());            
        LOG.info("=>" + request.getAttribute("msg"));            
        printParams(request);
    }
    
    private void printParams(HttpServletRequest request) {
        Enumeration<String> anames = request.getAttributeNames();
        while (anames.hasMoreElements())
        {
            String aname = anames.nextElement();
            LOG.info("attribute {} = {}", aname, request.getAttribute(aname));
        }
        Enumeration<String> pnames = request.getParameterNames();
        while (pnames.hasMoreElements())
        {
            String pname = pnames.nextElement();
            LOG.info("Param {} = {}", pname, request.getParameter(pname));
        }
    }

    //
    // --- Implements MonitListenerIntf ---------------------------------------------------------
    //
    @Override
    public void connect() {
        webServer = new Server(port);
        try {
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            webServer.setHandler(context);
     
            context.addServlet(new ServletHolder(this),"/*");

            webServer.start();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disconnect() {
        LOG.info("Stopping the embedded webserver ...");
        try {
            webServer.stop();
            LOG.info("Embedded webserver stopped.");
        } catch (Exception e) {
            LOG.warn("Failed to stop web server, continue anyway.", e);
        }
    }

    @Override
    public void setProcessor(MonitEventProcessor proc) {
        this.eventQueue = proc.getQueue();
    }


}
