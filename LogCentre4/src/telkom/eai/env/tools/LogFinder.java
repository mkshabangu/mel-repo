package telkom.eai.env.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

public class LogFinder {

	private HashMap<String, String> ENVs;

	private Properties properties;
	private static final boolean printdebugs=true;
	private int max_rec_count=250;
	
	private String vSearchBy;
	
	private static LogFinder instance;
	
	private LogFinder(String username,String password) {
		init(username,password);
	}
	
	private LogFinder() {
		init();
	}
	
	private LogFinder(Properties props) {
		this.properties = props;
		init();
	}
	
	public static LogFinder getInstance(String username, String password) {
		if(instance==null)
			instance = new LogFinder(username,password);
		
		return instance;
	}
	
	public static LogFinder getInstance() {
		if(instance==null)
			instance = new LogFinder();		
		return instance;
	}
	
	public static LogFinder getInstance(Properties props) {
		if(instance==null)
			instance = new LogFinder(props);

		return instance;
	}
	
	
	
	
	public HashMap<String, String> getENVs() {
		return ENVs;
	}
	
	public String getMatchinFileList(String searchey, String env, String filepattern) {	
				
		String cmd = "grep -l '"+searchey+"' " +this.getLogPath(env)+filepattern;
		print(cmd);
		return executeJSchCommand(env,cmd);
	}
	
	public String getMatchinFileList(String searchKey, String paramType, String env, String filepattern) {	
				
		if("MessageId".equalsIgnoreCase(paramType))
			searchKey="="+searchKey+"\\|MessageId>"+searchKey;
		else if("CorrelationId".equalsIgnoreCase(paramType))
			searchKey = "CorrelationId>"+searchKey;
		else if("InternalMessageId".equalsIgnoreCase(paramType))
			searchKey = "InternalMessageId>"+searchKey;
		else if("OrderActionID".equalsIgnoreCase(paramType))
			searchKey = "\\<OrderAction.*ID=\\\""+searchKey+"\\\"";
		
		//String cmd = "LC_ALL=C grep -l '"+searchKey+"' " +this.getLogPath(env, domain)+filepattern;
		String cmd = "find "+this.getLogPath(env)+filepattern+" | xargs -P 8 -L 1 grep -l '"+searchKey+"'";
		print(cmd);
		return executeJSchCommand(env,cmd);
	}
	
	private String createSearchKey(String searchKey, String env) {
		String key="";
		
		if (env.startsWith("EIB_")) {
			
			if("MessageId".equalsIgnoreCase(this.vSearchBy))
				key = "<MessageId>" + searchKey + "\\|:MessageId>"+searchKey;
			else if("CorrelationId".equalsIgnoreCase(this.vSearchBy))
				key = "CorrelationId>"+searchKey;
			else if("InternalMessageId".equalsIgnoreCase(this.vSearchBy))
				key="InternalMessageId>"+searchKey;
			else if("OrderActionID".equalsIgnoreCase(this.vSearchBy))
				key = "\\<OrderAction.*ID=\\\""+searchKey+"\\\"";
		}		
		else if (env.startsWith("EIESB_")) {
			if ("MessageId".equalsIgnoreCase(this.vSearchBy))
				key = "<MessageId>" + searchKey + "\\|:MessageId>"+searchKey;
			else if ("CorrelationId".equalsIgnoreCase(this.vSearchBy))
				key = "CorrelationId>" + searchKey + "\\|:CorrelationId>"+searchKey;
			else if ("InternalMessageID".equalsIgnoreCase(this.vSearchBy))
				key = "InternalMessageId>" + searchKey;
			else if ("OrderActionID".equalsIgnoreCase(this.vSearchBy))
				key = "OrderActionID>" + searchKey + "\\|SOMOrderID>"+searchKey;			
		}
		else {
			return null;
		}
		
		return key;
	}
	
	public String getMatchinFileList(String searchKey, String paramType, String env, String [] filepattern) {	
		this.vSearchBy = paramType;
		searchKey = createSearchKey(searchKey,env);		
		String multiFileOrExp=null;
		for(String pattern : filepattern) {
			if(multiFileOrExp==null) {
				multiFileOrExp="-iname "+pattern;
			}
			else {
				multiFileOrExp+=" -o -iname "+pattern;
			}
		}
		String multiFilePattern="\\( "+multiFileOrExp+ " \\)";
				
		//String cmd = "grep -l '"+searchKey+"' " +this.getLogPath(env, domain)+filepattern;
		String cmd = "LC_ALL=C find "+this.getLogPath(env)+" -type f "+multiFilePattern+" | xargs -P 8 -L 1 grep -l \""+searchKey+"\"";
		print(cmd);
		return executeJSchCommand(env,cmd);
	}
		
	public String [] getLog4jStartPositionsCsv(String env, String absFilePath, String start, String end){
				
		long startLineNumber = Long.parseLong(start);
		long endLineNumber = Long.parseLong(end);
		
		//String cmd = "LC_ALL=C grep -n \"\\*\\*\\*\\*\\*\\*\\*\\* \\|START_EIB_LOG_ENTRY\" " +absFilePath +" | cut -d : -f 1";
		String cmd = "LC_ALL=C sed -n '"+startLineNumber+","+endLineNumber+"p' "+absFilePath+" | LC_ALL=C grep -n \"\\*\\*\\*\\*\\*\\*\\*\\* \\|START_EIB_LOG_ENTRY\" " +" | cut -d : -f 1";
		
		print(cmd);
		//return executeShellCommand(cmd);
		String result = executeJSchCommand(env,cmd);
		
		
		String [] positions = result.split("\\r?\\n");
		
		for(int i=0;i<positions.length;++i) {
			positions[i] = ""+(Long.parseLong(positions[i])+startLineNumber);
			
		}
		
		return positions;
	}
	
	public String [] getLog4jEndPositionsCsv(String env, String absFilePath) {
		String cmd = "grep -n \"END_EIB_LOG_ENTRY\" " +absFilePath+" | cut -d : -f 1";
		print(cmd);
		String result = executeJSchCommand(env,cmd);
		return result.split("\\r?\\n");
	}
	
	public String [] getLog4jEndNDCPositionsCsv(String env, String absFilePath) {
		String cmd = "grep -n \"NDC=\" " +absFilePath+" | cut -d : -f 1";
		print(cmd);
		String result = executeJSchCommand(env,cmd);		
		return result.split("\\r?\\n");
	}
	
	
	public String [] getKeyPositionsCsv(String searchkey, String env, String absFilePath){
		String cmd = "LC_ALL=C grep -n \""+this.createSearchKey(searchkey, env)+"\" " +absFilePath+" | cut -d : -f 1";
		
		/*if(env.startsWith("EIESB_"))			cmd = "LC_ALL=C grep -n '"+searchkey+"' " +absFilePath+" | cut -d : -f 1";*/
		print(cmd);
		String result = executeJSchCommand(env,cmd);
		return result.split("\\r?\\n");
	}
	
	public String [] getESBLogEntryPositionsCsv(String env, String absFilePath){
		
		//String cmd = "LC_ALL=C grep -n \":AuditEvent xmlns\\|</ns0:AuditEvent>\" " +absFilePath+" | cut -d : -f 1";
		String cmd = "LC_ALL=C grep -n \":AuditEvent xmlns\\|</ns0:AuditEvent>\" " +absFilePath+" | cut -d : -f 1";
		
		
		print(cmd);
		String result = executeJSchCommand(env,cmd);
		
		return result.split("\\r?\\n");
	}
	
	public String [] getESBLogEntryPositionsCsv(String env, String absFilePath, String start, String end){
		
		long startLineNumber = Long.parseLong(start);
		long endLineNumber = Long.parseLong(end);
		
		String cmd = "LC_ALL=C sed -n '"+startLineNumber+","+endLineNumber+"p' "+absFilePath+" | grep -n \":AuditEvent xmlns\\|</ns0:AuditEvent>\" " +" | cut -d : -f 1";
		print(cmd);
		String result = executeJSchCommand(env,cmd);
		
		String [] positions = result.split("\\r?\\n");
		
		for(int i=0;i<positions.length;++i) {
			positions[i] = ""+(Long.parseLong(positions[i])+startLineNumber);
			
		}
		
		return positions;
	}
	
	public String [] getESBLogEntryPositionsCsv1(String env, String absFilePath, String start, String end){
		
		long startLineNumber = Long.parseLong(start)-1;
		long endLineNumber = Long.parseLong(end);
		
		String cmd = "LC_ALL=C sed -n '"+startLineNumber+","+endLineNumber+"p' "+absFilePath+" | grep -n -o \"AuditEvent xmlns\\|AuditEvent>\"";
		print(cmd);
		String result = executeJSchCommand(env,cmd);
		
		String [] positions = result.split("\\r?\\n");
		ArrayList<String> refinedList = new ArrayList<String>();
		
		String START_TAG="AuditEvent xmlns";
		String END_TAG="AuditEvent>";
		String previous_tag="";
		String prevPos="";
		String currPos="";
		
		for(int i=0;i<positions.length;++i) {
			
			currPos = ""+(Long.parseLong(positions[i].split(":")[0])+startLineNumber);
			
			if(i<5)
				print(positions[i]);
			
			if(previous_tag.length()==0 && (positions[i].indexOf(END_TAG) >= 0)){
				print("****Got rid of END_TAG at line"+((Long.parseLong(positions[i].split(":")[0])+startLineNumber)));
				continue;
			}
			
			if(i==0) {
				if(positions[i].indexOf(END_TAG) >= 0) {
					continue;
				}
			}
			else if(i==positions.length-1) {
				if(positions[i].indexOf(START_TAG) >= 0) {
					break;
				}
			}
			
			if(positions[i].indexOf(START_TAG) >= 0 && START_TAG.equals(previous_tag) && previous_tag.length()>0) {
				refinedList.remove(prevPos);
				print("****Got rid of START_TAG at line"+((Long.parseLong(positions[i].split(":")[0])+startLineNumber)));
				continue;
			}
			
			if(positions[i].indexOf(END_TAG) >= 0 && END_TAG.equals(previous_tag) && previous_tag.length()>0) {
				continue;
			}
			
			
			refinedList.add(currPos);
			prevPos = ""+(Long.parseLong(positions[i].split(":")[0])+startLineNumber);
			
			if(positions[i].indexOf(START_TAG) >= 0) {
				previous_tag = START_TAG;
			}else
			{
				previous_tag = END_TAG;
			}
			
		}
		
		String [] finalList = new String[refinedList.size()];
		refinedList.toArray(finalList);
		return finalList;
	}

	public String extractLogs(String env, String absFilePath, String searchkey) {
		if(env.startsWith("EIB_")) {
			return extractLogsEIB(env, absFilePath, searchkey);
		}
		else if(env.startsWith("EIESB_")) {
			return extractLogsESB(env, absFilePath, searchkey);
		}
		else {
			return "Error: Unrecognized environment: "+env;
		}
	}
	public String extractLogsEIB(String env, String absFilePath, String searchkey) {
		
        
        
		try {
			String [] keypos = getKeyPositionsCsv(searchkey,env,absFilePath);
			
			long start = Long.parseLong(keypos[0]);
			
			start = start>100?start-100:1;
			long end = Long.parseLong(keypos[keypos.length-1]) +400;
			
			String [] starts = getLog4jStartPositionsCsv(env,absFilePath,start+"",end+"");
			
			//String [] starts = getLog4jStartPositionsCsv(env,absFilePath);

			
			
			print("*********\n\n# total log entries:"+starts.length+"\n\n*********\n");
			String cmd="LC_ALL=C sed -n ";
			HashSet<String> uniquePositionsSet = new HashSet<String>();
			int lastmatch=0;
			for(int i=0; i<keypos.length;++i) {
				for(int j=lastmatch;j<starts.length;++j) {
					
					int startindex = Integer.parseInt(starts[j]);
					int endindex = 0;
					int keyindex = Integer.parseInt(keypos[i]);
					if(j<starts.length-1) {
						endindex = (Integer.parseInt(starts[j+1])-1);
					}
					else {
						continue;
					}
					if(keyindex>startindex && endindex>keyindex) {
						
						if(j<starts.length-1) {
							uniquePositionsSet.add(starts[j]+","+(Integer.parseInt(starts[j+1])-1)+"p");
						}
						else {
							continue;//uniquePositionsSet.add(starts[j]+","+ends[j]+"p");
						}
						lastmatch = j;
						break;
					}
				}
			}
			
			Iterator<String> li = uniquePositionsSet.iterator();
			String filterPositions="";
			
			while(li.hasNext()) {
				
				if("".equals(filterPositions)) {
					filterPositions = li.next();
				}
				else {
					filterPositions += ";"+li.next();
				}				
			}
						
			if(!"".equals(filterPositions)) {
				cmd += "'"+filterPositions+"' "+absFilePath;
				print(cmd);
				String logs = executeJSchCommand(env,cmd);
				return logs;
			}
		} catch (Throwable e) {
			print(e);
		}

		return "";
		
	}
	
	public String extractLogsESB(String env, String absFilePath, String searchkey) {
		
        String infomessage="";
        
		try {
			String [] keypos = getKeyPositionsCsv(searchkey,env,absFilePath);
			
			long start = Long.parseLong(keypos[0]);
			
			start = start>100?start-100:1;
			long end = Long.parseLong(keypos[keypos.length-1]) +400;
			
			if((end-start) > 10000000)
				start = end/2;
			
			String [] logEntryPositions = getESBLogEntryPositionsCsv1(env,absFilePath,start+"",end+"");
			
			String cmd="sed -n ";
			
			ArrayList<String> positionList = new ArrayList<String>();
			int lastmatch=0;
			for (int i = 0; i < keypos.length; i++) {
				int keyindex = Integer.parseInt(keypos[i]);
				for (int j = lastmatch; j < logEntryPositions.length; j+=2) {

					int pos = Integer.parseInt(logEntryPositions[j]);
					pos = pos>2?pos-2:pos;

					if (j < (logEntryPositions.length - 1)) {
						
						int nextPos = Integer.parseInt(logEntryPositions[j + 1]);
						
						if(keyindex > pos && keyindex > nextPos)
							continue;
						else if(keyindex >= pos && keyindex < nextPos){
							
							String pair = pos+","+(--nextPos)+"p";
							if(!positionList.contains(pair))
								positionList.add(pair);
							lastmatch = j;
							break;
						}
						else {
							break;
						}
						
					}
				} 
			}			
			
			if(positionList.size() > this.max_rec_count) {
				infomessage = positionList.size()+" entries matched exceeds limit of "+this.max_rec_count+" entries! Extracted the last "+this.max_rec_count+" entries. \n\n";
				positionList = new ArrayList<String>(positionList.subList(positionList.size()-this.max_rec_count, positionList.size()));
				
				print(infomessage);
			}
			else {
				infomessage = "Number of entries extracted: "+positionList.size()+"\n\n";
				print(infomessage);
			}			
			
			Iterator<String> li = positionList.iterator();
			String filterPositions="";
			
			while(li.hasNext()) {
				
				if("".equals(filterPositions)) {
					filterPositions = li.next();
				}
				else {
					filterPositions += ";"+li.next();
				}				
			}			
			
			if(!"".equals(filterPositions)) {
				cmd += "'"+filterPositions+"' "+absFilePath;
				print(cmd);
				String logs = executeJSchCommand(env,cmd);
				return infomessage+logs;
			}
		} catch (Throwable e) {
			print(e);
		}

		return "Could not extract logs for searck key "+searchkey;		
	}

	static void print(Object o) {
		
		if(!printdebugs) return;
		
		if(o instanceof Throwable) {
			Throwable e = (Throwable) o;
			for(StackTraceElement ste : e.getStackTrace()) {
				System.out.println(ste.toString());
			}
		}
		else {
			System.out.println(o);
		}
		
		
	}
	
	/*public String extractLogs(String env, String absFilePath, String searchkey) {
		
        
        
		try {
			String [] keypos = getKeyPositionsCsv(searchkey,env,absFilePath);
			String [] starts = getLog4jStartPositionsCsv(env,absFilePath);
			String [] ends = getLog4jEndPositionsCsv(env,absFilePath);
			if(ends == null || ends.length<=0 || "".equals(ends[0])) {
				ends = getLog4jEndNDCPositionsCsv(env,absFilePath);
			}
			System.out.println("*********\n\n# total log entries:"+starts.length+"\n\n*********\n");
			String cmd="sed -n ";
			HashSet<String> uniquePositionsSet = new HashSet<String>();
			int lastmatch=0;
			for(int i=0; i<keypos.length;++i) {
				for(int j=lastmatch;j<starts.length;++j) {
					
					int startindex = Integer.parseInt(starts[j]);
					int endindex = Integer.parseInt(ends[j]);
					int keyindex = Integer.parseInt(keypos[i]);
					if(keyindex>startindex && endindex>keyindex) {
						
						if(j<starts.length-1)
							uniquePositionsSet.add(starts[j]+","+(Integer.parseInt(starts[j+1])-1)+"p");
						else
							uniquePositionsSet.add(starts[j]+","+ends[j]+"p");	
						lastmatch = j;
						break;
					}
				}
			}
			
			Iterator<String> li = uniquePositionsSet.iterator();
			String filterPositions="";
			
			while(li.hasNext()) {
				
				if("".equals(filterPositions)) {
					filterPositions = li.next();
				}
				else {
					filterPositions += ";"+li.next();
				}				
			}			
			
			if(!"".equals(filterPositions)) {
				cmd += "'"+filterPositions+"' "+absFilePath;
				System.out.println(cmd);
				String logs = executeJSchCommand(env,cmd);
				return logs;
			}
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
		
	}*/
	
	//*******************************************************************************************************************
	
	private String getLogPath(String env) {
		return properties.getProperty(env+"_logpath");
		
	}
	
	
	
	private void init(String username, String password) {
		init();
	}
	
	
	private String getEnvHost(String env) {
		return this.properties.getProperty(env+"_host");
	}
	
	
	private String getEnvUser(String env) {
		return this.properties.getProperty(env+"_username");
	}
	
	private String getEnvPwd(String env) {
		return this.properties.getProperty(env+"_password");
	}
	
	private void init() {
		
		String csvEnvs = properties.getProperty("env_names");
		String [] env_array = csvEnvs.split(",");
		ENVs = new HashMap<String, String>();
		for(String env : env_array) {
			ENVs.put(env, properties.getProperty(env+"_host"));
		}
		
		String rec_count = properties.getProperty("MAX_LOG_COUNT");
		
		if(rec_count != null) {
			if(!rec_count.trim().equals("")) {
				try {
					this.max_rec_count = Integer.parseInt(rec_count);
				} catch (NumberFormatException e) {
					this.max_rec_count = 250;
				}			
			}
		}
	}
	
	public void setLogin(String uname, String passwd) {
		this.init(uname,passwd);
	}
	
	public String testAccess(String env) {
		
		return executeJSchCommand(env,"echo 'success'");
	}
	
	private String executeJSchCommand(String env, String cmd){
		
		return JschUtil.excecuteCommand(this.getEnvHost(env), this.getEnvUser(env), this.getEnvPwd(env), cmd);
	
	}
}
