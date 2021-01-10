package io.infolayer.aida.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PluginMetadata implements Cloneable {
	
	private String name;
	private String platform;
	private String description;
	private List<PluginParameterMetadata> parameters;
	
	public PluginMetadata() { }
	
	public PluginMetadata(String name, String platform, String description, List<PluginParameterMetadata> params) {
		this.name = name;
		this.platform = platform;
		this.description = description;
		this.parameters = params;
	}

	public String getName() {
		return name;
	}

	public String getPlatform() {
		return platform;
	}

	public String getDescription() {
		return description;
	}
	
	public List<PluginParameterMetadata> getParameters() {
		return this.parameters;
	}

	public void addParameter(PluginParameterMetadata parameter) {
		this.parameters.add(parameter);
	}

	public PluginParameterMetadata getParameter(String name) {
		if (name != null) {
			for (PluginParameterMetadata p : parameters) {
				if (p.getName().equals(name)) {
					return p;
				}
			}
		}
		return null;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setParameters(List<PluginParameterMetadata> parameters) {
		this.parameters = parameters;
	}
	
	@Override
	public PluginMetadata clone() throws CloneNotSupportedException {
		if (parameters == null) {
			return new PluginMetadata(name, platform, description, null);
			
		}else {
			List<PluginParameterMetadata> params = new ArrayList<PluginParameterMetadata>();
			this.parameters.forEach((param) -> {
				try {
					params.add(param.clone());
				} catch (CloneNotSupportedException e) { }
			});
			
			return new PluginMetadata(name, platform, description, params);
			
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(description, name, parameters, platform);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PluginMetadata other = (PluginMetadata) obj;
		return Objects.equals(description, other.description) && Objects.equals(name, other.name)
				&& Objects.equals(parameters, other.parameters) && Objects.equals(platform, other.platform);
	}

	@Override
	public String toString() {
		return String.format("PluginMetadata [name=%s, platform=%s, description=%s, parameters=%s]", name,
				platform, description, parameters);
	}
	
	
	
}