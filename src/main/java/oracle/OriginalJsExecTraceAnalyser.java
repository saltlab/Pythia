package oracle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import domMutation.Node;

import executionTracer.DOM_JS_ExecutionTracer;
import executionTracer.JSExecutionTracer;

public class OriginalJsExecTraceAnalyser extends JsExecTraceAnalyser{
	
	public static Multimap<String, FunctionState> funcNameToFuncStateMap;

	private Multimap<String, FunctionPoint> funcNameToFuncPointMap;
	public static ArrayList<String> functionListOfOriginalVersion=new ArrayList<String>();
	public static HashMap<String, ArrayListMultimap<FunctionPoint,FunctionPoint>> funcEntryPointToExitPointMap=new HashMap<String, ArrayListMultimap<FunctionPoint,FunctionPoint>>();
	
	public OriginalJsExecTraceAnalyser(String outputFolder){
		
		
		super(outputFolder);
		createFuncEntryToFuncExitMap();
	}
	

	public Multimap<String, FunctionState> getFuncNameToFuncStateMap(){
		return funcNameToFuncStateMap;
	}
	
	@Override
	protected List<String> allTraceFiles() {
		ArrayList<String> result = new ArrayList<String>();

		/* find all trace files in the trace directory */
		File dir = new File(outputFolder +  DOM_JS_ExecutionTracer.EXECUTIONTRACEDIRECTORY);

		String[] files = dir.list();
		if (files == null) {
			return result;
		}
		for (String file : files) {
			if (file.endsWith(".txt")) {
				result.add(outputFolder + DOM_JS_ExecutionTracer.EXECUTIONTRACEDIRECTORY + file);
			}
		}

		return result;
	}
	
	@Override
	protected void startAnalysingJsExecTraceFiles(){
		try{
			funcNameToFuncStateMap=ArrayListMultimap.create();
			funcNameToFuncPointMap=ArrayListMultimap.create();
			List<String>filenameAndPathList=getTraceFilenameAndPath();
			List<Object[]> funcListToSort=	new ArrayList<Object[]>();
			for (String filenameAndPath:filenameAndPathList){
				BufferedReader input =
					new BufferedReader(new FileReader(filenameAndPath));
				
				String line="";
				while ((line = input.readLine()) != null){
					
					String[] funcNameLine=line.split(":::");
					String funcName=funcNameLine[0];
					String pointName=funcNameLine[1];
					long time=0;
					String variableName="";
					String type="";
					String value="";
					String variableUsage="";
					String domHtml="";
/*					String nodeLine="";
					String nodeValue="";
*/					
					Variable varibale;
//					AccessedDOMNode accessedDomNode=null;

					ArrayList<Variable> variables=new ArrayList<Variable>();
					ArrayList<AccessedDOMNode> domNodes=new ArrayList<AccessedDOMNode>();
					
					FunctionPoint functionPoint;
//					FunctionPoint domRelatedFunctionPoint;
				
					while (!(line = input.readLine()).equals
							("===========================================================================")){

						if(line.contains("time::")){
						
							time=Long.valueOf(line.split("::")[1]);
						}
						else if(line.contains("variable::")){
							variableName=line.split("::")[1];
						}
						else if(line.contains("type::")){
							type=line.split("::")[1];
						}
						else if(line.contains("value::")){
							value=line.split("::")[1];
						}
						
						else if(line.contains("variableUsage::")){
							variableUsage=line.split("::")[1];
						}
						else if(line.contains("dom::")){
							domHtml=line.split("dom::")[1];
						}
						else if(line.contains("node::")){
							String node=line.split("node::")[1];
							ObjectMapper mapper = new ObjectMapper();
						    AccessedDOMNode domNode = mapper.readValue(node, AccessedDOMNode.class);  
						    mapper.writeValueAsString(domNode);
						    domNode.makeAllAttributes();
						    domNodes.add(domNode);
						
						}
/*						else if(line.contains("line::")){
							nodeLine=line.replace("line::", "");
						}
						else if(line.contains("value::")){
							nodeValue=line.replace("value::", "");
						}
						
*/						
						
						
						if(variableName!="" && value!="" && type!="" && variableUsage!=""){
							varibale=new Variable(variableName, value, type, variableUsage);
							variables.add(varibale);
							variableName="";
							value="";
							type="";
							variableUsage="";
							
							
						}
/*						if(domNode!=null){
							accessedDomNode=new AccessedDOMNode(domNode);
							
						}
*/						
					
					}
					
/*					if(accessedDomNode!=null){
						addingDomRelatedFunctionPoint(accessedDomNode, funcName, pointName, time);
					}
					else{
						addingNonDomRelatedFunctionPoint(variables, funcName, pointName, time);
					}
*/					
					
					
//					List<FunctionPoint> functionPoints=(List<FunctionPoint>) funcNameToFuncPointMap.get(funcName);
//					java.util.Collections.sort(functionPoints, bvc);

					
				functionPoint=new FunctionPoint(pointName, variables, domHtml, time);

				Object[] funcPointFuncName=new Object[2];
				funcPointFuncName[0]=functionPoint;
				funcPointFuncName[1]=funcName;
								
				funcListToSort.add(funcPointFuncName);
				java.util.Collections.sort(funcListToSort, funcNameFuncPointComp);
				
				if(domNodes.size()>0){
					functionPoint.addAccessedDomNodes(domNodes);
				}
		//		funcNameToFuncPointMap.put(funcName, functionPoint);
				}
				input.close();
			  }
			
			for(int i=0; i<funcListToSort.size();i++){
				FunctionPoint fPoint=(FunctionPoint) funcListToSort.get(i)[0];
				String fName=(String) funcListToSort.get(i)[1];
				if(fPoint.getPointName().toLowerCase().equals("enter")){
					for(int j=i+1; j<funcListToSort.size(); j++){
						FunctionPoint fNextPoint=(FunctionPoint) funcListToSort.get(j)[0];
						String fNextName=(String) funcListToSort.get(j)[1];
						if(fNextName.equals(fName) && fNextPoint.getPointName().toLowerCase().equals("exit")){
							break;
						}
						else 
							if(!fNextName.equals(fName) && fNextPoint.getPointName().toLowerCase().equals("enter")){
								fPoint.addGlobVariableIfNotExist(fNextPoint.getVariables());
						//		fPoint.addAccessedDomNodes(fNextPoint.getAccessedDomNodes());
								
							}
					}
				}
			}
			
			for(int i=0;i<funcListToSort.size();i++){
				FunctionPoint fPoint=(FunctionPoint) funcListToSort.get(i)[0];
				String fName=(String) funcListToSort.get(i)[1];
				List<FunctionPoint> funcList=(List<FunctionPoint>) funcNameToFuncPointMap.get(fName);
		
				funcNameToFuncPointMap.put(fName, fPoint);
		
			}
			
			Set<String> keys=funcNameToFuncPointMap.keySet();
			Iterator<String> it=keys.iterator();
			while(it.hasNext()){
				String funcName=it.next();
				List<FunctionPoint> points=(List<FunctionPoint>) funcNameToFuncPointMap.get(funcName);
				java.util.Collections.sort(points, vc);
			}
			
	/*		Set<String> keysets=funcNameToFuncPointMap.keySet();
			it=keysets.iterator();
			while(it.hasNext()){
				String funcName=it.next();
				Collection<FunctionPoint> points=funcNameToFuncPointMap.get(funcName);
				Iterator<FunctionPoint> iter=points.iterator();
				
				while(iter.hasNext()){
					System.out.println(funcName + iter.next().getTime());
				}
			}
	*/	}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void createFuncNameToFuncStateMap(){
		Set<String> funcNames=funcNameToFuncPointMap.keySet();
		Iterator<String> iter=funcNames.iterator();
		while(iter.hasNext()){
			String funcName=iter.next();
			List<FunctionPoint> funcPoints=new ArrayList<FunctionPoint>((List<FunctionPoint>) funcNameToFuncPointMap.get(funcName));
			int i=0;
			while(i<funcPoints.size()){
				FunctionState funcState;
				FunctionPoint entry=null;
				FunctionPoint exit=null;
				FunctionPoint funcPoint=funcPoints.get(i);
				String pointName=funcPoint.getPointName();
				if(pointName.toLowerCase().equals("enter")){
					if(funcPoints.size()==1){
						funcPoints.remove(entry);
						i=0;
						break;
					}
					if(funcPoints.get(i+1).getPointName().toLowerCase().equals("enter")){
					
						for(int count=i+1;count<funcPoints.size();count++){
							
							if(funcPoints.get(count).getPointName().toLowerCase().equals("exit")){
							
									entry=funcPoints.get(count-1);
									exit=funcPoints.get(count);
									funcPoints.remove(entry);
									funcPoints.remove(exit);
									i=0;
									break;							
								
							}
						}
					}
					else{
						entry=funcPoint;
						for(int j=i;j<funcPoints.size();j++){
							FunctionPoint point=funcPoints.get(j);
							if(point.getPointName().toLowerCase().equals("exit")){
								exit=point;
								funcPoints.remove(entry);
								funcPoints.remove(exit);
								i=0;
								break;
							}
							
						}
					}
					funcState=new FunctionState(entry, exit);
					List<FunctionState> fStates=(List<FunctionState>) funcNameToFuncStateMap.get(funcName);
					int test=0;
					if(entry!=null && exit==null){
						
						exit=new FunctionPoint("exit", entry.getVariables(), entry.getDomHtml(), entry.getTime());
						funcPoints.remove(entry);
						i=0;
					}
					if(entry==null && exit==null)
						break;
					if(!fStates.contains(funcState) && entry!=null && exit!=null)
						funcNameToFuncStateMap.put(funcName, funcState);
				}
				else{
					entry=funcPoint;
					exit=funcPoints.get(i-1);
					funcPoints.remove(entry);
					funcPoints.remove(exit);
					i=0;
					funcState=new FunctionState(entry, exit);
					List<FunctionState> fStates=(List<FunctionState>) funcNameToFuncStateMap.get(funcName);
					if(!fStates.contains(funcState)  && entry!=null && exit!=null)
						funcNameToFuncStateMap.put(funcName, funcState);
				}
			}
		}
	}
	

	
	private void createFuncEntryToFuncExitMap(){
		
		Set<String> keys=funcNameToFuncStateMap.keySet();
		Iterator<String> iter=keys.iterator();
		while(iter.hasNext()){
			String funcName=iter.next();
			ArrayList<FunctionState> funcStates=new ArrayList<FunctionState>(funcNameToFuncStateMap.get(funcName));
		//	ArrayList<FunctionState> funcStates=(ArrayList<FunctionState>) ((ArrayList<FunctionState>) funcStatestemp).clone();
			
			ArrayListMultimap<FunctionPoint,FunctionPoint> funcPointMltimap=ArrayListMultimap.create();
			Iterator<FunctionState> fStOuterIter=funcStates.iterator();
			while(fStOuterIter.hasNext()){
				
				FunctionState funcState=fStOuterIter.next();
				FunctionPoint funcEntry=funcState.getFunctionEntry();
				FunctionPoint funcExit=funcState.getFunctionExit();
				ArrayList<Variable> varList=funcEntry.getVariables();
				funcPointMltimap.put(funcEntry, funcExit);
				funcStates.remove(funcState);
				Iterator<FunctionState> fStIter=funcStates.iterator();
				while(fStIter.hasNext()){
					FunctionState fState=fStIter.next();
					FunctionPoint nextFuncEntry=fState.getFunctionEntry();
					ArrayList<Variable> nextVarList=nextFuncEntry.getVariables();
					
					if(nextVarList.equals(varList)){
						FunctionState nextFuncState=fState;
						FunctionPoint nextFuncExit=nextFuncState.getFunctionExit();
						List<FunctionPoint> exitPointList=funcPointMltimap.get(funcEntry);
						boolean similar=false;
						for(int count=0;count<exitPointList.size();count++){
							if(functionPointsSimilar(exitPointList.get(count),nextFuncExit)){
								similar=true;
								break;
							
							}
						}
						if(!similar){
							
							funcPointMltimap.put(funcEntry, nextFuncExit);
							
						}
						funcStates.remove(fState);
						fStIter=funcStates.iterator();
						
					}
					
		/*			if(nextVarList.size()==varList.size()){
						for(int k=0;k<nextVarList.size();k++){
							if(!nextVarList.get(k).equals(varList.get(k))){
								equal=false;
								break;
							}
						}
					}
		*/			
					
				}
				fStOuterIter=funcStates.iterator();
				
			}
			funcEntryPointToExitPointMap.put(funcName, funcPointMltimap);
			
		}
	}
	
	
	
	
	private boolean functionPointsSimilar(FunctionPoint funcPoint1, FunctionPoint funcPoint2){
		if(funcPoint1.getPointName().equals(funcPoint2.getPointName())){
			ArrayList<Variable> varList1=funcPoint1.getVariables();
			ArrayList<Variable> varList2=funcPoint2.getVariables();
			if(varList1.equals(varList2))
				return true;
		}
		return false;
	}
	
	public static void makeFunctionListOfOriginalVersion(){
		Set<String> keys=funcNameToFuncStateMap.keySet();
		Iterator<String> iter=keys.iterator();
		while(iter.hasNext()){
			functionListOfOriginalVersion.add(iter.next());
		}
	
	}
	
/*	private void addingDomRelatedFunctionPoint(AccessedDOMNode accessedDomNode, String funcName, String pointName, long time){
		List<FunctionPoint> currentFuncNameToFuncPointMap=(List<FunctionPoint>) funcNameToFuncPointMap.get(funcName);
		if(currentFuncNameToFuncPointMap!=null){
			java.util.Collections.sort(currentFuncNameToFuncPointMap, vc);
			FunctionPoint lastPoint=currentFuncNameToFuncPointMap.get(currentFuncNameToFuncPointMap.size()-1);
			if(lastPoint.getPointName().equals("enter")){
				lastPoint.addAccessedDomNode(accessedDomNode);
			}
			else{
				FunctionPoint functionPoint=new FunctionPoint(pointName, accessedDomNode, time);
				funcNameToFuncPointMap.put(funcName, functionPoint);
			}
		}
		else{
			FunctionPoint functionPoint=new FunctionPoint(pointName, accessedDomNode, time);
			funcNameToFuncPointMap.put(funcName, functionPoint);
		}
	}
	
	private void addingNonDomRelatedFunctionPoint(ArrayList<Variable> variables, String funcName, String pointName, long time){
		List<FunctionPoint> currentFuncNameToFuncPointMap=(List<FunctionPoint>) funcNameToFuncPointMap.get(funcName);
		if(currentFuncNameToFuncPointMap!=null){
			java.util.Collections.sort(currentFuncNameToFuncPointMap, vc);
			FunctionPoint lastPoint=currentFuncNameToFuncPointMap.get(currentFuncNameToFuncPointMap.size()-1);
			if(lastPoint.getPointName().equals("enter")
					&& lastPoint.getAccessedDomNodes().size()>0
					&& lastPoint.getVariables().size()==0){
				lastPoint.addVariable(variables);
			}
			else{
				FunctionPoint functionPoint=new FunctionPoint(pointName, variables, time);
				funcNameToFuncPointMap.put(funcName, functionPoint);
			}
		}
		else{
			FunctionPoint functionPoint=new FunctionPoint(pointName, variables, time);
			funcNameToFuncPointMap.put(funcName, functionPoint);
		}
	}
*/
}
