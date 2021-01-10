package io.infolayer.aida.executor.runnable;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Properties;

import org.osgi.service.jdbc.DataSourceFactory;

import io.infolayer.aida.annotation.Plugin;
import io.infolayer.aida.annotation.PluginParameter;
import io.infolayer.aida.entity.PluginCall;
import io.infolayer.aida.exception.PluginException;
import io.infolayer.aida.executor.util.OsgiUtils;
import io.infolayer.aida.executor.util.ParameterValueResolver;
import io.infolayer.aida.plugin.IPluginService;
import io.infolayer.aida.plugin.OutputFlow;
import io.infolayer.aida.plugin.PluginSubmissionResponse;

@Plugin(name = "jdbc.connect", description = "Connect to a JDBC Datasource")
public class JdbcConnectPlugin extends AbstractRunnablePlugin {

    @PluginParameter(name = "uri", required = true)
    private String connectionUri;

    @PluginParameter(name = "driver", required = true)
	private String connectionDriver;

	@Override
	public Object execute() throws Exception {
		return null;
	}

	@Override
	public void run() {
		
		lifecycleRunning();

		final OutputFlow outputFlow = this.getOutputFlow(this);

		Connection conn = null;
		
		try {
			
			outputFlow.attach(this);
			outputFlow.addAllEnvironments(this.getEnvironment());
			
			// if (this.inputHandlers != null) {
			// 	for (IPluginInputHandler input : inputHandlers) {
			// 		input.proccess(getPlugin(), this.getParametersValue(), getOutputFlow(this));
			// 	}
			// }
			
			/*
			 * PLUGIN RUNNING CODE
			 * PLUGIN RUNNING CODE 
			 * PLUGIN RUNNING CODE 
			 * PLUGIN RUNNING CODE 
			 * PLUGIN RUNNING CODE 
			 */

			try {
			
				String connUri = ParameterValueResolver.updateText(connectionUri, outputFlow, this.getEnvironment(), this.getParametersValue(), null);
				
				//System.out.println(connUri);
				
				/*
				 * JDBC Requires special case to deal With JDBC Drivers
				 */
				if (OsgiUtils.isOSGIFramework()) {
					DataSourceFactory dsf = OsgiUtils.getOSGIDataSourceFactory(connectionDriver, true);
					
					Driver driver = dsf.createDriver(null);
					
					Properties props = new Properties();
					props.put("user", this.getParameter("credentials_key").getValue());
					props.put("password", this.getParameter("credentials_secret").getValue());
					
					DriverManager.setLoginTimeout(this.getTimeout());
					conn = driver.connect(connUri, props);
					
				}else {
					Class.forName(connectionDriver).newInstance();
					DriverManager.setLoginTimeout(this.getTimeout());
					conn = DriverManager.getConnection(connUri, this.getParameter("credentials_key").getValue(),
							this.getParameter("credentials_secret").getValue());
				}
				
				if (conn == null) {
					throw new PluginException("JDBC Null connection.");
				}

				if (!isDryRun()) {

					if (getCall().getCall() != null) {

						logDebug(MessageFormat.format("Looping execution chain for plugin instance {0}",
								this.getInstanceID()));
						for (PluginCall innerCall : getCall().getCall()) {
	
							PluginCall innerCallClone = innerCall.clone();
							
							/*
							 * Resolv Parameters
							 */
							ParameterValueResolver.updateMap(innerCallClone.getPluginParams(), outputFlow, getEnvironment(),
									this.getParametersValue(), conn);
	
							try {
								IPluginService service = OsgiUtils.getOSGIService(IPluginService.class, true);
								PluginSubmissionResponse response = service.submit(innerCallClone, getEnvironment(),
										getListeners(), outputFlow);
	
							} catch (Exception e) {
								logError("Got exception inside plugin call. BUG ! I cannot handle this... {} ", e.getMessage());
								return;
							}
	
						}
					}

				}
				
			} finally {
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					logWarn("Exception while closing connection: {}", e.getMessage());
				}
			}
			
			/*
			 * PLUGIN RUNNING CODE
			 * PLUGIN RUNNING CODE 
			 * PLUGIN RUNNING CODE 
			 * PLUGIN RUNNING CODE 
			 * PLUGIN RUNNING CODE 
			 */
			
			lifecycleSuccess(null);
			
		}catch (InterruptedException e) {
			
			if (isDebugEnabled()) {
				e.printStackTrace();
			}
			
			lifecycleInterrupted();
			
		} catch (Exception e) {

			if (isDebugEnabled()) {
				e.printStackTrace();
			}
			
			lifecycleException(e);
			
		} finally {
			outputFlow.detach(this);
		}
		
	}
}