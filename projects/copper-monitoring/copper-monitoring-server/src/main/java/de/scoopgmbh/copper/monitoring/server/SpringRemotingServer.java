/*
 * Copyright 2002-2013 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.scoopgmbh.copper.monitoring.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.apache.shiro.spring.remoting.SecureRemoteInvocationExecutor;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import de.scoopgmbh.copper.monitoring.core.CopperMonitoringService;
import de.scoopgmbh.copper.monitoring.core.LoginService;

public class SpringRemotingServer {
	
	static final Logger logger = LoggerFactory.getLogger(SpringRemotingServer.class);
	private Server server;
	private final CopperMonitoringService copperMonitoringService;
	private final int port;
	private final String host; 
	private final DefaultLoginService loginService;
	
	public SpringRemotingServer(CopperMonitoringService copperMonitoringService, int port, String host, DefaultLoginService loginService) {
		super();
		this.copperMonitoringService = copperMonitoringService;
		this.port = port;
		this.host = host;
		this.loginService = loginService;
	}

	public void start() {
		logger.info("Starting Copper-Monitor-Server (jetty)");

		server = new Server();
		SocketConnector connector = new SocketConnector();
		connector.setPort(port);
		connector.setHost(host);
		server.setConnectors(new Connector[] { connector });

		ServletContextHandler servletContextHandler = new ServletContextHandler(server, "/", true, false);
	
		//Servlet adress is defined with the bean name
		//try to avoid xml config (dont sacrifice type safety)
		GenericWebApplicationContext genericWebApplicationContext = new GenericWebApplicationContext();
		genericWebApplicationContext.registerBeanDefinition("/loginService",
				BeanDefinitionBuilder.genericBeanDefinition(HttpInvokerServiceExporter.class).
				addPropertyValue("service", loginService).
				addPropertyValue("serviceInterface", LoginService.class.getName()).
				getBeanDefinition());
		genericWebApplicationContext.registerBeanDefinition("/copperMonitoringService",
				BeanDefinitionBuilder.genericBeanDefinition(HttpInvokerServiceExporter.class).
				addPropertyValue("service", copperMonitoringService).
				addPropertyValue("serviceInterface", CopperMonitoringService.class.getName()).
				addPropertyValue("remoteInvocationExecutor", createSecureRemoteInvocationExecutor()).
				getBeanDefinition());
		genericWebApplicationContext.refresh();
		
		DispatcherServlet dispatcherServlet = new DispatcherServlet(genericWebApplicationContext);
		ServletHolder servletHolder = new ServletHolder(dispatcherServlet);
		servletContextHandler.addServlet(servletHolder, "/*");
		
//		FilterHolder filterHolder = new FilterHolder();
//		GzipFilter filter = new GzipFilter();
//		filterHolder.setFilter(filter);
//		EnumSet<DispatcherType> types = EnumSet.allOf(DispatcherType.class);
//		servletContextHandler.addFilter(filterHolder, "/**", types);
		
		HandlerCollection handlers = new HandlerCollection();
		final RequestLogHandler requestLogHandler = new RequestLogHandler();
		NCSARequestLog requestLog = new NCSARequestLog();
		requestLog.setAppend(true);
		requestLog.setExtended(true);
		requestLog.setLogLatency(true);
		requestLogHandler.setRequestLog(requestLog);
		handlers.setHandlers(new Handler[] {servletContextHandler, requestLogHandler});
		server.setHandler(handlers);
		
	
		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private SecureRemoteInvocationExecutor createSecureRemoteInvocationExecutor(){
		final SecureRemoteInvocationExecutor secureRemoteInvocationExecutor = new SecureRemoteInvocationExecutor();
		return secureRemoteInvocationExecutor;
	}

	private void stop() {
		try {
			server.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) {
		if (!new File(args[0]).exists() || args.length==0){
			throw new IllegalArgumentException("valid property file loctaion must be passed as argument invalid: "+(args.length>0?new File(args[0]).getAbsolutePath():"nothing"));
		}
		
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(args[0]));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		final Integer port = Integer.valueOf((String) properties.getProperty("webapp.jetty.listener.port"));
		final String host = (String)properties.getProperty("webapp.jetty.host");
		
		PropertyConfigurator.configure(args[1]);
		System.setProperty("org.apache.cxf.Logger", "org.apache.cxf.common.logging.Log4jLogger");
		
		
		SpringRemotingServer springRemoteServerMain = new SpringRemotingServer(null,port,host,null);
		try {
			springRemoteServerMain.start();
			System.in.read();
			springRemoteServerMain.stop();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isRunning() {
		return server.isRunning();
	}

}