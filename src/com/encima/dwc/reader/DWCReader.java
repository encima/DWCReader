package com.encima.dwc.reader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gbif.dwc.record.DarwinCoreRecord;
import org.gbif.dwc.record.Record;
import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.ArchiveField;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.dwc.text.StarRecord;
import org.gbif.dwc.text.UnsupportedArchiveException;

public class DWCReader {
	
	String path = null;
	
	public String getPath() {
		return path;
	}


	public void setPath(String path) {
		this.path = path;
	}


	public DWCReader(String path) {
		this.setPath(path);
		File f = new File(path);
		if(f.exists()) {
			System.out.println("Archive loaded");
		}else{
			System.out.println("Archive not found, set path again");
			this.setPath(null);
		}
	}

	
	public HashMap<ConceptTerm, String> extractArchive() {
		if(this.getPath() != null) {
			HashMap<ConceptTerm, String> record = new HashMap<ConceptTerm, String>();
			try {
				Archive arch = ArchiveFactory.openArchive(new File("path"));
				System.out.println("Reading archive from "+ arch.getLocation().getAbsolutePath());
			    System.out.println("Archive of rowtype "+ arch.getCore().getRowType() +" with "+ arch.getExtensions().size() + " extensions");
			    
			    // loop over star records. i.e. core with all linked extension records
			    Iterator<StarRecord>iter = arch.iterator();
			    Set<ConceptTerm> coreTerms  = arch.getCore().getTerms();
			    
			    while(iter.hasNext()) {
			    	//Core Terms
			    	StarRecord sr = iter.next();
			    	Iterator<ConceptTerm> termsIter= coreTerms.iterator();
			    	while(termsIter.hasNext()) {
			    		ConceptTerm ct = termsIter.next();
			    		record.put(ct, sr.core().value(ct).toString());
			    	}
			    	//This loads all the extension types and can be used to get the elusive terms hidden within the extension files.(While loop may not be necessary as the base template only has/needs one extension.
			    	Iterator<String> rowTypes = sr.rowTypes().iterator();
			    	List<Record> ext;
			    	if(sr.rowTypes().size() == 1 && sr.rowTypes().size() > 0) {
			    		ext = sr.extension(rowTypes.next());
			    		for(int i = 0; i < ext.size(); i++) {
			    			Iterator<ConceptTerm> extTerms = ext.get(i).terms().iterator();
			    			while(extTerms.hasNext()) {
			    				ConceptTerm ct = extTerms.next();
			    				if(!record.containsKey(ct)) {
			    					record.put(ct, ext.get(i).value(ct));
			    				} else {
			    					StringBuilder locs = new StringBuilder();
			    					locs.append(record.get(ct)).append(",");
			    					locs.append(ext.get(i).value(ct));
			    					record.put(ct,  locs.toString());
			    				}
			    			}
			    		}
			    	}else{
			    		//add code for multiple records, split above into method seems best
			    	}
			    }
			} catch (UnsupportedArchiveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return record;
		}else{
			System.out.println("Archive could not be loaded");
			return null;
		}
		
	}
		
	
  public static void main(String[] args) throws IOException, UnsupportedArchiveException {
	  //opens csv files with headers or dwc-a direcotries with a meta.xml descriptor
	  DWCReader dr = new DWCReader("/Users/encima/Dropbox/Projects/PhD/Darwin_Core_Files/dwc_arch");
	  HashMap<ConceptTerm, String> record = dr.extractArchive();
	  System.out.println(record);
  }     
}
