package runner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import oracle.DomStateComparator;
import oracle.FunctionPoint;
import oracle.FunctionStateComparator;
import oracle.JsExecTraceAnalyser;
import oracle.MutatedJsExecTraceAnalyser;
import oracle.Oracle;
import oracle.OriginalJsExecTraceAnalyser;
import qunitGenerator.QunitTestSuite;


import astModifier.JSModifyProxyPlugin;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.core.configuration.ThreadConfiguration;
import com.crawljax.path.AllPath;
import com.crawljax.path.DOMElement;
import com.crawljax.path.Globals;
import com.crawljax.plugins.proxy.WebScarabWrapper;
import com.crawljax.plugins.testcasegenerator.TestSuiteGenerator;
import com.crawljax.util.Helper;
import com.google.common.collect.ArrayListMultimap;

import domMutation.DomMuteHelper;
import domMutation.Dom_Mut_Analyser;
import domMutation.Dom_OrigMut_Analyser;
import domMutation.Dom_Orig_Analyser;

import executionTracer.AstInstrumenter;
import executionTracer.DOMAstInstrumenter;
import executionTracer.DOMExecutionTracer;
import executionTracer.DOMMutAstInstrumenter;
import executionTracer.DOMMuteExecutionTracer;
import executionTracer.DOMOrigExecutionTracer;
import executionTracer.DOM_JS_AstInstrumenter;
import executionTracer.DOM_JS_ExecutionTracer;
import executionTracer.JSExecutionTracer;

public class SameGameDomMutation {
	
	private static final String URL = "http://localhost:8080//same-game/same-game.htm";	
	/* No limit on max depth or max state*/
	private static final int MAX_DEPTH = 0;
	private static final int MAX_NUMBER_STATES = 0;

	private SameGameDomMutation() {

	}

	/**
	 * Main method.
	 * 
	 * @param args
	 *            Arguments.
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {


		String outputdir = "same-output";

//		Dom_Mut_Analyser dom_Mut_Analyser=new Dom_Mut_Analyser(outputdir);

	

//		System.setProperty("webdriver.firefox.bin" ,"/ubc/ece/home/am/grads/shabnamm/program-files/firefox18/firefox/firefox");
		CrawljaxConfiguration config = getCrawljaxConfiguration();
		config.setOutputFolder(outputdir);


//		ProxyConfiguration prox = new ProxyConfiguration();
//		WebScarabWrapper web = new WebScarabWrapper();
		config.addPlugin(new TestSuiteGenerator());
	
	/*	Dom_Mut_Analyser domMutAnalyser=new Dom_Mut_Analyser(outputdir);
		Dom_Orig_Analyser domOrigAnalyser=new Dom_Orig_Analyser(outputdir);
		DomStateComparator domComparator=new DomStateComparator(outputdir);
		domComparator.analysingOutPutDiffs();
	*/	
		
		
//		DOMMutAstInstrumenter a;
//		DOMAstInstrumenter b=new DOMAstInstrumenter();
//		DOMAstInstrumenter a=new DOMAstInstrumenter();
//		DomMuteHelper helper=new DomMuteHelper(outputdir);
//		ArrayList<DOMMutAstInstrumenter> dommutes=helper.domMutAstInstrumenterGenerator();
		String stateName="";
	//	for(int i=0;i<2;i++){
//			a=dommutes.get(2);
//			stateName=a.getStateName();
//			JSModifyProxyPlugin p = new JSModifyProxyPlugin(a);
//			p.excludeDefaults();
//			web.addPlugin(p);
	//	}
/*		JSModifyProxyPlugin p = new JSModifyProxyPlugin(b);
		p.excludeDefaults();
		web.addPlugin(p);
*/		
//		DOMExecutionTracer tracer=new DOMExecutionTracer("domExecutiontrace");		
//		DOMMuteExecutionTracer tracer = new DOMMuteExecutionTracer("domMuteExecutiontrace",stateName);
//		JSExecutionTracer tracer = new JSExecutionTracer("jsExecutionTrace");
//		DOMOrigExecutionTracer tracer = new DOMOrigExecutionTracer("domOrigExecutiontrace",stateName);
/*		tracer.setOutputFolder(outputdir);
		config.addPlugin(tracer);
		config.addPlugin(web);
		config.setProxyConfiguration(prox);
*/

		try {
			CrawljaxController crawljax = new CrawljaxController(config);
			String filenameAndPath =  Helper.addFolderSlashIfNeeded(outputdir) + "allPossiblePath" + ".txt";
			ArrayList<AllPath> allPath=readAllPossiblePathFile(filenameAndPath);
			
			for(int i=0;i<allPath.size();i++){
				Globals.allPath=allPath.get(0);
				crawljax.run();
				break;
			}
				

		} catch (Exception e) {
			e.printStackTrace();
		}
		
/*		
		String outputdir = "same-output2";
		DomTraceReading trace=new DomTraceReading(outputdir);
*/		

	}
	
	private static CrawlSpecification getCrawlSpecification() {
		CrawlSpecification crawler = new CrawlSpecification(URL);
		

		// crawler.setMaximumRuntime(300); 		
		

		// click these elements
		boolean tudu = false; 

		if (!tudu){
			//defining clickables
	
	/*		crawler.click("a");
			crawler.click("div");
			crawler.click("span");
			crawler.click("img");
			crawler.click("input").withAttribute("type", "submit");
			crawler.click("div");
	*/		crawler.click("td");
			crawler.setWaitTimeAfterEvent(2000);
		}else{
			// this is just for the TuduList application
			Form form=new Form();
			Form addList=new Form();
			form.field("j_username").setValue("shabnam");
			form.field("j_password").setValue("shabnam");
			form.field("dueDate").setValue("10/10/2010");
			form.field("priority").setValue("10");
			//addList.field("description").setValue("test");
			InputSpecification input = new InputSpecification();
			input.setValuesInForm(form).beforeClickElement("input").withAttribute("type", "submit");
			input.setValuesInForm(addList).beforeClickElement("a").withAttribute("href", "javascript:addTodo();");
			crawler.setInputSpecification(input);
			crawler.click("a");
			crawler.click("img").withAttribute("id", "add_trigger_calendar");
			crawler.click("img").withAttribute("id", "edit_trigger_calendar");
			
			//crawler.click("a");
			crawler.click("div");
			crawler.click("span");
			crawler.click("img");
			//crawler.click("input").withAttribute("type", "submit");
			crawler.click("td");

			crawler.dontClick("a").withAttribute("title", "My info");
			crawler.dontClick("a").withAttribute("title", "Log out");
			crawler.dontClick("a").withAttribute("text", "Cancel");
		}


		// except these
		crawler.dontClick("a").underXPath("//DIV[@id='guser']");
		crawler.dontClick("a").withText("Language Tools");
		
		if (!tudu)
			crawler.setInputSpecification(getInputSpecification());

		crawler.setClickOnce(true);		
		crawler.setMaximumStates(MAX_NUMBER_STATES);
		crawler.setDepth(MAX_DEPTH);

		return crawler;
	}

	
	private static CrawljaxConfiguration getCrawljaxConfiguration() {
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		config.setCrawlSpecification(getCrawlSpecification());
		config.setThreadConfiguration(getThreadConfiguration());
		config.setBrowser(BrowserType.firefox);
		return config;
	}
	
	private static ThreadConfiguration getThreadConfiguration() {
		ThreadConfiguration tc = new ThreadConfiguration();
		tc.setBrowserBooting(true);
		tc.setNumberBrowsers(1);
		tc.setNumberThreads(1);
		return tc;
	}
	
	
	private static InputSpecification getInputSpecification() {
		InputSpecification input = new InputSpecification();
		input.field("q").setValue("Crawljax");
		return input;
	}
	
	private static ArrayList<AllPath> readAllPossiblePathFile(String filenameAndPath){
		ArrayList<AllPath> allPossiblePath=new ArrayList<AllPath>();
		try {
			BufferedReader input =
					new BufferedReader(new FileReader(filenameAndPath));
			String line="";
			while((line = input.readLine()) != null){
				
				String[] str=line.split("::");
				String startVertex=str[0];
				String endVertex=str[1];
				AllPath allPath=new AllPath(startVertex,endVertex);
				while(true){
					String attributeName="";
					String attributeValue="";
					String elementName="";
				//	ArrayList<ElementAttribute> attributes=new ArrayList<ElementAttribute>();
					DOMElement domElement = new DOMElement();
					while(!(line=input.readLine()).equals("---------------------------------------------------------------------------")){
						if(line.equals("===========================================================================")){
							allPossiblePath.add(allPath);
							break;
						}
						if(line.contains("tagName::")){
							elementName=line.split("::")[1];
							domElement.setDOMElementName(elementName);
						}
						else{
							String[] attr=line.split("::");
							attributeName=attr[0];
							attributeValue=attr[1];
				//			ElementAttribute attribute=new ElementAttribute(attributeName, attributeValue);
							domElement.setAttributes(attributeName, attributeValue);
				//			attributes.add(attribute);
						}
					}
					
					if(line.equals("===========================================================================")){
			
						break;
					}
			//		DOMElement domElement=new DOMElement(elementName, attributes);
					allPath.pushToQueue(domElement);
				
				}
			
				
			}
			input.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		return allPossiblePath;
	}
	


}
