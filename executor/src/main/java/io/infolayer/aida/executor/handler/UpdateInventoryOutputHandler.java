//package io.infolayer.siteview.plugins.handler;
//
//import java.text.MessageFormat;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import io.infolayer.siteview.annotation.ConfiguredParameter;
//import io.infolayer.siteview.annotation.SiteviewService;
//import io.infolayer.siteview.entity.InventoryItem;
//import io.infolayer.siteview.entity.InventoryItemType;
//import io.infolayer.siteview.exception.OutputHandlerException;
//import io.infolayer.siteview.exception.RepositoryException;
//import io.infolayer.siteview.plugin.IPluginOutputHandler;
//import io.infolayer.siteview.plugin.OutputFlow;
//import io.infolayer.siteview.repository.IInventoryRepository;
//import io.infolayer.siteview.util.InventoryItemBuilder;
//
//public class UpdateInventoryOutputHandler implements IPluginOutputHandler {
//	
//	private static Logger log = LoggerFactory.getLogger(UpdateInventoryOutputHandler.class);
//	
//	@SiteviewService
//	public IInventoryRepository inventoryRepository;
//	
//	/*
//	 * Sintaxe:
//	 * field1,field2,field3:renamed_to_field4
//	 */
//	@ConfiguredParameter
//	public String includeFields;
//	
//	@ConfiguredParameter
//	public String excludeFields;
//	
//	@ConfiguredParameter
//	public String primaryKey;
//	
//	@ConfiguredParameter
//	public String inventoryType;
//	
//	private boolean prepared = false;
//	
//	private boolean includeAll = false;
//	private Map<String,String> include = null;
//	private Map<String,String> exclude = null;
//	
//	@Override
//	public void prepare() throws OutputHandlerException {
//		
//		if (this.inventoryRepository == null) {
//			this.prepared = false;
//			log.error("IEntityRepository instance is null. Unable to persist.");
//			return;
//		}
//		
//		if (this.includeFields != null && this.includeFields.equals("")) {
//			this.includeFields = null;
//			this.includeAll = true;
//		}
//		
//		if (this.excludeFields != null && this.excludeFields.equals("")) {
//			this.excludeFields = null;
//		}
//		
//		if (this.primaryKey != null && this.primaryKey.equals("")) {
//			throw new OutputHandlerException("primaryKey must be set for UpdateInventoryOutputHandler");
//		}
//		
//		include = new HashMap<String,String>();
//		exclude = new HashMap<String,String>();
//		
//		if (this.includeFields != null) {
//			//field1,field2,field3:renamed_to_field4
//			String[] includeArray = this.includeFields.split(",");
//			for (int i = 0; i < includeArray.length; i++) {
//				String item = includeArray[i];
//				if (item.contains(":")) {
//					String[] name = item.split(":");
//					include.put(name[0].trim(), name[1].trim());
//				}else {
//					include.put(item.trim(), null);
//				}
//			}
//		}
//		
//		if (this.excludeFields != null) {
//			String[] excludeArray = this.excludeFields.split(",");
//			for (int i = 0; i < excludeArray.length; i++) {
//				String item = excludeArray[i];
//				if (item.contains(":")) {
//					String[] name = item.split(":");
//					include.put(name[0].trim(), name[1].trim());
//				}else {
//					include.put(item.trim(), null);
//				}
//			}
//		}
//		
//		this.prepared = true;
//	}
//	
//	@Override
//	public boolean isPrepared() {
//		return prepared;
//	}
//
//	@Override
//	public void proccess(OutputFlow flow, boolean abort) throws OutputHandlerException {
//		
//		if (flow == null) {
//			return;
//		}
//		
//		Map<String, Object> props = flow.getPropoerties();
//		Map<String, String> env = flow.getEnvironment();
//		Map<String, Object> result = new ConcurrentHashMap<String, Object>();
//		
//		for (Map.Entry<String, Object> entry : props.entrySet()) {
//			
//			String fieldName = this.getFieldName(entry.getKey());
//			if (fieldName != null) {
//				result.put(fieldName, entry.getValue());
//			}
//			
//		}
//		
//		for (Map.Entry<String, String> entry : env.entrySet()) {
//			
//			String fieldName = this.getFieldName(entry.getKey());
//			if (fieldName != null) {
//				result.put(fieldName, entry.getValue());
//			}
//			
//		}
//		
//		/*
//		 
//		 [Result]
//		 	- Control fields.
//		 	server		Siteview Instance (owner)				UUID
//		 	ID			Record's ID								UUID
//		 	firstSeen	First time the record has been seen.	Timestamp
//		 	lastSeen	Latest time the record has been seen.	Timestamp
//		 
//		 	- All other Fields from result Map					<Objects>
//		 
//		 */
//		
//		try {
//			
//			InventoryItemType itemType = inventoryRepository.getInventoryItemType(inventoryType);
//			
//			if (itemType == null) {
//				throw new OutputHandlerException("Unknow inventory type " + inventoryType);
//			}
//			
//			log.info(MessageFormat.format("Inventory update: Plugin ({0}) is updating item. Primary key is: {1}", 
//					env.get("plugin.name"),
//					this.primaryKey));
//			
//			InventoryItem item = InventoryItemBuilder.create()
//				.setIgnoreNull(true)
//				.setInventoryItemType(itemType.getName())
//				.build(result);
//			
//			this.inventoryRepository.save(item);
//			
//		} catch (RepositoryException e) {
//			throw new OutputHandlerException(e.getMessage());
//		}
//		
//	}
//	
//	private String removeDots(String name) {
//		if (name != null) {
//			return name.replaceAll("\\.", "_");
//		}
//		return null;
//	}
//	
//	/**
//	 * Return Null if field must not be stored.
//	 * @param name
//	 * @return
//	 */
//	private String getFieldName(String name) {
//		
//		if (this.includeAll) {
//			return removeDots(name);
//		}
//		
//		if (name != null) {
//			
//			//Is excluded?
//			if (this.exclude.containsKey(name)) {
//				return null;
//			}
//			
//			return removeDots(this.include.get(name));
//		
//		}
//		
//		return null;
//	}
//
//
//}
