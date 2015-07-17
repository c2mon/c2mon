
package cern.c2mon.daq.monit.listener;

import java.io.IOException;
import java.util.Queue;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
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
        LOG.info("GET target: {}", request.getQueryString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>Hello World</h1>");
        response.getWriter().println("session=" + request.getSession(true).getId());
        LOG.info("POST target: {}", request.getQueryString());
    }

    //
    // --- Implements MonitListenerIntf ---------------------------------------------------------
    //
    @Override
    public void connect() {
        webServer = new Server(port);
        try {
            webServer.start();
            
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            webServer.setHandler(context);
     
            context.addServlet(new ServletHolder(this),"/*");
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
