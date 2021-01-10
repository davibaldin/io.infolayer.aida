package io.infolayer.aida.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PluginCall implements Cloneable {
	
	private String pluginName;
	private Map<String,String> pluginParams;
	private List<PluginCall> call;
	
	public PluginCall() {
		pluginParams = new HashMap<String, String>();
	}

	public PluginCall(String pluginName) {
		pluginParams = new HashMap<String, String>();
		this.pluginName = pluginName;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public Map<String, String> getPluginParams() {
		return pluginParams;
	}

	public List<PluginCall> getCall() {
		return call;
	}
	
	public void setCall(List<PluginCall> call) {
		this.call = call;
	}
	
	public void addPluginCall(PluginCall call) {
		if (call != null) {
			if (this.call == null) {
				this.call = new ArrayList<PluginCall>();
			}
			this.call.add(call);
		}
	}

	public synchronized void addPluginParam(String name, String value) {
		if (name != null) {
			if (this.pluginParams == null) {
				this.pluginParams = new HashMap<String, String>();
			}
			this.pluginParams.put(name, value);
		}
	}
	
	public void setPluginParams(Map<String, String> pluginParams) {
		this.pluginParams = pluginParams;
	}

	@Override
	public String toString() {
		return String.format("PluginCall [pluginName=%s, pluginParams=%s, call=%s]", pluginName, pluginParams, call);
	}

	@Override
	public int hashCode() {
		return Objects.hash(call, pluginName, pluginParams);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PluginCall other = (PluginCall) obj;
		return Objects.equals(call, other.call) && Objects.equals(pluginName, other.pluginName)
				&& Objects.equals(pluginParams, other.pluginParams);
	}
	
	@Override
	public PluginCall clone() {
		
		PluginCall clone = new PluginCall();
		clone.setPluginName(this.pluginName);
		
		if (this.pluginParams != null) {
			this.pluginParams.forEach((k,v) -> {
				clone.addPluginParam(k, v);
			});
		}
		
		if (this.call != null) {
			this.call.forEach((item) -> {
				clone.addPluginCall(item.clone());
			}); 
		}
		
		return clone;
		
	}

}
