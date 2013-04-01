package domMutation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.crawljax.util.Helper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import executionTracer.DOMMuteExecutionTracer;

public class Dom_OrigMut_Analyser {
	/*<stateName-xpath->attribute1, attribute2,...>*/
	private Multimap<String, DomAttribute> stateXpathToNodeAttrsMap=ArrayListMultimap.create();
	private String outputFolder;
	private List<String> traceFilenameAndPath;
	
	public Dom_OrigMut_Analyser(String outputFolder){
	
		this.outputFolder=Helper.addFolderSlashIfNeeded(outputFolder);
		traceFilenameAndPath=allTraceFiles();
		startReadingDom_OrigMut_TraceFiles();
		
	}
	
	private List<String> allTraceFiles() {
		ArrayList<String> result = new ArrayList<String>();

		/* find all trace files in the trace directory */
		File dir = new File(outputFolder +  DOMMuteExecutionTracer.EXECUTIONTRACEDIRECTORY);

		String[] files = dir.list();
		if (files == null) {
			return result;
		}
		for (String file : files) {
			if (file.endsWith(".txt")) {
				result.add(outputFolder + DOMMuteExecutionTracer.EXECUTIONTRACEDIRECTORY + file);
			}
		}

		return result;
	}
	
	public Multimap<String, DomAttribute> getstateXpathToNodeAttrsMap(){
		return stateXpathToNodeAttrsMap;
	}
	
	public List<String> getTraceFilenameAndPath() {
		return traceFilenameAndPath;
	}
	
	private void startReadingDom_OrigMut_TraceFiles(){
		try{
			List<String>filenameAndPathList=getTraceFilenameAndPath();
			for (String filenameAndPath:filenameAndPathList){
				BufferedReader input =
					new BufferedReader(new FileReader(filenameAndPath));
			
				String state_xpath="", inputline="";
				inputline=input.readLine();
				String stateName=inputline.replace("state::", "");
				input.readLine();
				while ((inputline = input.readLine()) != null){
			
				if ("".equals(inputline))
					break;
				
				String xpath="";
				String tagName=inputline.split("::")[1];
				DomAttribute attr=new DomAttribute("tagName", tagName);
					while (!(inputline = input.readLine()).equals("===========================================================================")){

						if(inputline.contains("xpath::")){
							xpath=inputline.split("::")[1];
							state_xpath=stateName+"_"+xpath;
							stateXpathToNodeAttrsMap.put(state_xpath, attr);
						}
						else {
							String attrName=inputline.split("::")[0];
							String attrValue=inputline.split("::")[1];
							DomAttribute domAttr=new DomAttribute(attrName, attrValue);
							stateXpathToNodeAttrsMap.put(state_xpath, domAttr);
						}
					}
	
				
		
				}
			  
			 input.close();
			}
		
		
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
}


