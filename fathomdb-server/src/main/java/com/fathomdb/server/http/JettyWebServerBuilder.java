package com.fathomdb.server.http;

import java.io.File;
import java.net.InetAddress;
import java.security.KeyStore;
import java.util.EnumSet;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

import com.fathomdb.crypto.CertificateAndKey;
import com.fathomdb.crypto.EncryptionStore;
import com.fathomdb.crypto.KeyStoreUtils;
import com.fathomdb.crypto.ssl.AcceptAllClientCertificatesTrustManager;
import com.fathomdb.crypto.ssl.SslPolicy;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;

public class JettyWebServerBuilder implements WebServerBuilder {
    @Inject(optional = true)
    EncryptionStore encryptionStore;

    final Server server;

    final HandlerCollection handlers;
    final ContextHandlerCollection contexts;

    public JettyWebServerBuilder() {
        this.server = new Server();
        this.handlers = new HandlerCollection();
        this.contexts = new ContextHandlerCollection();
        this.handlers.addHandler(this.contexts);
        this.server.setHandler(this.handlers);
    }

    @Override
    public Server start() throws Exception {
        if (server.getThreadPool() == null) {
            server.setThreadPool(buildThreadPool());
        }

        server.start();
        return server;
    }

    protected ThreadPool buildThreadPool() {
        return new QueuedThreadPool();
    }

    @Override
    public void addHttpConnector(InetAddress address, int port, boolean async) {
        Connector connector;
        if (async) {
            connector = buildSelectChannelConnector(address, port);
        } else {
            connector = buildSocketConnector(address, port);
        }

        // connector.setHost("127.0.0.1");
        server.addConnector(connector);
    }

    @Override
    public void addHttpConnector(int port, boolean async) {
        addHttpConnector(null, port, async);
    }

    protected Connector buildSocketConnector(InetAddress address, int port) {
        SocketConnector connector = new SocketConnector();
        if (address != null) {
            connector.setHost(address.getHostAddress());
        }
        connector.setPort(port);
        return connector;
    }

    protected Connector buildSelectChannelConnector(InetAddress address, int port) {
        SelectChannelConnector connector = new SelectChannelConnector();
        if (address != null) {
            connector.setHost(address.getHostAddress());
        }
        connector.setPort(port);
        return connector;
    }

    @Override
    public void enableRequestLogging() {
        NCSARequestLog requestLog = new NCSARequestLog("./weblogs/jetty-yyyy_mm_dd.request.log");
        // requestLog.setRetainDays(90);
        requestLog.setAppend(true);
        requestLog.setExtended(false);
        requestLog.setLogLatency(true);
        requestLog.setLogDispatch(true);
        requestLog.setLogTimeZone("GMT");

        RequestLogHandler requestLogHandler = new RequestLogHandler();
        requestLogHandler.setRequestLog(requestLog);
        handlers.addHandler(requestLogHandler);
    }

    public ServletContextHandler addContext(String path) {
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");

        contexts.addHandler(decorateContext(context));

        return context;
    }

    protected Handler decorateContext(ServletContextHandler context) {
        return context;
    }

    @Override
    public void addGuiceContext(String path, Injector injector) {
        GuiceServletConfig servletConfig = injector.getInstance(GuiceServletConfig.class);
        addGuiceContext(path, servletConfig);
    }

    @Override
    public void addGuiceContext(String path, GuiceServletContextListener servletConfig) {
        ServletContextHandler context = addContext(path);

        context.addEventListener(servletConfig);

        // Must add DefaultServlet for embedded Jetty
        // Failing to do this will cause 404 errors.
        context.addServlet(DefaultServlet.class, "/");

        FilterHolder filterHolder = new FilterHolder(GuiceFilter.class);
        context.addFilter(filterHolder, "*", EnumSet.of(DispatcherType.REQUEST));

        context.setClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Override
    public void addHttpsConnector(int port, Set<SslOption> options) throws Exception {
        addHttpsConnector(null, port, options);
    }

    @Override
    public void addHttpsConnector(InetAddress address, int port, Set<SslOption> options) throws Exception {
        SslContextFactory sslContextFactory;
        if (options.contains(SslOption.AllowAnyClientCertificate)) {
            CustomTrustManagerSslContextFactory customSslContextFactory = new CustomTrustManagerSslContextFactory();
            TrustManager[] trustManagers = new TrustManager[] { new AcceptAllClientCertificatesTrustManager() };
            customSslContextFactory.setTrustManagers(trustManagers);

            sslContextFactory = customSslContextFactory;
        } else {
            sslContextFactory = new SslContextFactory(SslContextFactory.DEFAULT_KEYSTORE_PATH);
        }

        // TODO: Preconfigure a better SSLContext??
        SSLContext sslContext = SSLContext.getDefault();
        sslContextFactory
                .setIncludeCipherSuites(SslPolicy.DEFAULT.getEngineConfig(sslContext).getEnabledCipherSuites());
        sslContextFactory.setIncludeProtocols(SslPolicy.DEFAULT.getEngineConfig(sslContext).getEnabledProtocols());

        {
            CertificateAndKey certificateAndKey = getCertificateAndKey();

            String secret = KeyStoreUtils.DEFAULT_KEYSTORE_SECRET;
            KeyStore keystore = KeyStoreUtils.createEmpty(secret);

            String alias = "https";

            KeyStoreUtils.put(keystore, alias, certificateAndKey, secret);

            sslContextFactory.setKeyStore(keystore);
            sslContextFactory.setKeyStorePassword(secret);
            sslContextFactory.setCertAlias(alias);
        }

        if (options.contains(SslOption.WantClientCertificate)) {
            sslContextFactory.setWantClientAuth(true);
        }

        SslSelectChannelConnector connector = new SslSelectChannelConnector(sslContextFactory);
        if (address != null) {
            connector.setHost(address.getHostAddress());
        }
        connector.setPort(port);
        server.addConnector(connector);
    }

    private CertificateAndKey getCertificateAndKey() throws Exception {
        if (encryptionStore == null) {
            throw new IllegalStateException("EncryptionStore must be bound");
        }

        return encryptionStore.getCertificateAndKey("https");
    }

    @Override
    public void addWar(String contextPath, File war) {
        final WebAppContext context = new WebAppContext();
        context.setWar(war.getAbsolutePath());
        contextPath = "/" + contextPath;
        context.setContextPath(contextPath);

        context.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

        contexts.addHandler(context);
    }

}
