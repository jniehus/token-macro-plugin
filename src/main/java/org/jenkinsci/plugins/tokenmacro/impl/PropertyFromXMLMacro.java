package org.jenkinsci.plugins.tokenmacro.impl;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import java.io.Closeable;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.w3c.dom.*;
import javax.xml.xpath.*;
import javax.xml.parsers.*;
import org.xml.sax.SAXException;
import org.jenkinsci.plugins.tokenmacro.DataBoundTokenMacro;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;

/**
 * Expands to a xml value(s) from a xml file relative to the workspace root.
 * If xpath evaluates to more than one value then a semicolon separted string is returned.
 */
@Extension
public class PropertyFromXMLMacro extends DataBoundTokenMacro {

    @Parameter(required=true)
    public String file = null;

    @Parameter(required=true)
    public String xpath = null;

    @Override
    public boolean acceptsMacroName(String macroName) {
        return macroName.equals("XML");
    }

    @Override
    public String evaluate(AbstractBuild<?, ?> context, TaskListener listener, String macroName) throws MacroEvaluationException, IOException, InterruptedException {
        String root = context.getWorkspace().getRemote();
        return context.getWorkspace().act(new ReadXML(root,file,xpath));
    }

    private static class ReadXML implements Callable<String,IOException> {

        private String root;
        private String filename;
        private String xpathexpression;
        
        public ReadXML(String root, String filename, String xpath){
            this.root=root;
            this.filename=filename;
            this.xpathexpression=xpath;
        }
        
        public String call() throws IOException {
            //Properties props = new Properties();
            File file = new File(root, filename);
            String xpathResult = "";
            if (file.exists()) {  
                try {
                    DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = dFactory.newDocumentBuilder();
                    Document doc = builder.parse(file.toString());
                    XPath xpathInstance = XPathFactory.newInstance().newXPath();
                    XPathExpression expr = xpathInstance.compile(xpathexpression);
                    // oh god, im lost in the Kingdom of Nouns!
                    
                    Object result = expr.evaluate(doc, XPathConstants.NODESET);
                    NodeList nodes = (NodeList) result;
                    for (int i = 0; i < nodes.getLength(); i++) {
                        xpathResult = xpathResult.concat(nodes.item(i).getNodeValue()).concat(";");
                    }
                    
                    xpathResult = xpathResult.substring(0, xpathResult.length() - 1); // trim the last ';'
                }
                catch (IOException e) {
                    xpathResult = "Error: ".concat(filename).concat(" - Could not read.");
                }
                catch (XPathExpressionException e) {
                    xpathResult = "Error: ".concat(xpathexpression).concat(" - Invalid syntax or path.");
                }
                catch (ParserConfigurationException e) {
                    xpathResult = "Error: ".concat(filename).concat(" - XML not well formed.");
                }
                catch (SAXException e) {
                    xpathResult = "Error: ".concat(filename).concat(" - XML not well formed.");
                }
                catch (Exception e) {
                    xpathResult = "Error: ".concat(filename).concat(" - '").concat(xpathexpression).concat("' invalid syntax or path maybe?");
                }
            }
            else {
                xpathResult = "Error: ".concat(filename).concat(" not found");
            }
            
            return xpathResult;
        }
    }
}