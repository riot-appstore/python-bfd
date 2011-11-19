package com.reverbin.ODA.server;

import java.io.*;
import java.util.regex.*;
import java.util.Arrays;

import com.reverbin.ODA.shared.DisassemblyOutput;
import com.reverbin.ODA.shared.Endian;
import com.reverbin.ODA.shared.Instruction;
import com.reverbin.ODA.shared.PlatformDescriptor;
import com.reverbin.ODA.shared.PlatformId;
import com.reverbin.ODA.shared.Instruction;


import com.reverbin.ODA.server.HostUtils;

import org.apache.commons.io.*;


public class Objdump
{		
    /**
     * Execute the given command line and return the captured stdout
     */
    private static String exec(String cmdline)
    {   /* ### TODO: This should eventually be a StringBuilder class for
                     efficiency reasons
        */
        String output = "";
        try
        {
            String line;
            Process p = Runtime.getRuntime().exec(cmdline);
            output = IOUtils.toString(p.getInputStream(), "UTF-8");
        }
        catch (Exception err)
        {
            err.printStackTrace();
        }
        return output;
    }

    /**
     * Convert the disassembly listing to pretty HTML
     */
    private static DisassemblyOutput dis2html(String dis, int offset, int length)
    {
    	// ignore leading text
    	Pattern pattern = Pattern.compile("^\\s*[0-9a-fA-F]+:.*", Pattern.DOTALL | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(dis);
        matcher.find();
        dis = matcher.group();
        final int MAX_LINES = 2000;
        int line = 0;
        int count = 0;
        int totalCount = 0;
        DisassemblyOutput output = new DisassemblyOutput();
        
        // Create pattern to identify and save errors in the disassembly
        //	ARM = <errortype>
        //	x86 - (bad)
        Pattern errorInstPattern = Pattern.compile("(<.+>|" + Pattern.quote("(bad)") + ")");
        Matcher errorInstMatcher;
        
        // now parse each line to get the offset, raw bytes and instruction
        pattern = Pattern.compile(
                    // beginning of the line plus white space
                    "^\\s*" +               
                    // offset and colon followed by white space
                    "([0-9a-f]*):\\s+" +    
                    // raw binary bytes "xxxxxxxx" or "xx xx xx .."
                    "([0-9a-f]{8}|(?:[0-9a-f]{2} )+|(?:[0-9a-f]{4} )+)" +
                    // instruction
                    "(.*)$",
                    Pattern.MULTILINE);
        matcher = pattern.matcher(dis);
                
        // for each match (each line, really)
        while (matcher.find())
        {       	
        		
        	totalCount++;
        	
        	if (line++ < offset) {
        		continue;
        	}
        	
        	if (count < length)
        	{
        		String instr = matcher.group(3).trim();
        		Instruction instruction = new Instruction();
        		
        		// Save Instruction Data
        		//	TODO: Save registers separately
        		instruction.address = Integer.parseInt(matcher.group(1), 16);
        		instruction.hexdata = matcher.group(2);
        		instruction.opcode = instr;
        		instruction.addressFmt = String.format("0x%08x", instruction.address);
        		
        		// Look for errors in the opcode
        		errorInstMatcher = errorInstPattern.matcher(instr);
        		if ( errorInstMatcher.find() )
        		{
        			instruction.isError = true;
        			instruction.errorType = errorInstMatcher.group(1);
        		}
        		
        		// Format hex data the same for all processors
        		//	(objdump doesn't do it)
        		instruction.hexdata = instruction.hexdata.replace(" ", ""); 
        		
        		// Store the meta data
        		output.addInstruction(instruction.address, instruction);
        			            
	        	count++;
        	}	        	
        }

        output.setTotalLines(totalCount);
        output.setCurrentLines(count);
        		
        return output;
        
    }

    private static String getPrefix(PlatformDescriptor platform)
    {    	
    	String prefix = "";
    	
    	switch (platform.platformId)
    	{
	    	case PPC: {
	    		prefix = "ppc-elf-";
	    		break;
	    	}
	    	case X86: {
	    		prefix = "i686-elf-";	    			
	    		break;
	    	}
	    	case ARM: {
	    		prefix = "arm-elf-";
	    		break;
	    	}
	    	case MIPS: {
	    		prefix = "mips-elf-";
	    		break;
	    	}
	    	case TMS320C6X: {
	    		prefix = "tic6x-elf-";
	    		break;
	    	}
	    	case TMS320C80: {
	    		prefix = "tic80-elf-";
	    		break;
	    	}
    	}    	
    
    	return prefix;
    }

    private static String getMachine(PlatformDescriptor platform)
    {    	
    	String machine = "";
    
		switch (platform.platformId)
		{
	    	case PPC: {
	    		machine = "powerpc";
	    		break;
	    	}
	    	case X86: {
	    		machine = "i386";
	    		break;
	    	}
	    	case ARM: {
	    		machine = "arm";
	    		break;
	    	}
	    	case MIPS: {
	    		machine = "mips";
	    		break;
	    	}
	    	case TMS320C6X: {
	    		machine = "tic6x";
	    		break;
	    	}
	    	case TMS320C80: {
	    		machine = "tic6x";
	    		break;
	    	}
		}
		
		return machine;
	}    	
    
    private static String buildDisExecStr(PlatformDescriptor platform, String filePath)
    {

    	//"objdump -D -b binary -m " + platform + " " + );
    	String prefix = getPrefix(platform);
    	String machine = getMachine(platform);    	
		String binutilsDir = HostUtils.getBinutilsDir();    		
    	String endian = "";
    	
    	switch (platform.endian)
    	{
    		case BIG:	endian = " -EB";		break;
    		case LITTLE:	endian = " -EL";	break;
    		case DEFAULT:	endian = "";	break;
    	}
    	
    	String option = "";
    	if ( platform.option != null )
    	{
	    	switch (platform.option)
	    	{
	    		case THUMB:	option = " -M force-thumb"; break;
	    		case NONE:	option = " "; break;
	    		case DEFAULT:	option = " "; break;
	    	}
    	}
    	
    	return  binutilsDir + prefix + "objdump -D -b binary -w -z -m " + machine + " --adjust-vma=" + platform.baseAddress + endian + " " + option + " " + filePath;
    }

    
    private static String buildSectionExecStr(PlatformDescriptor platform, String filePath)
    {

    	String prefix = getPrefix(platform);
		String binutilsDir = HostUtils.getBinutilsDir();    		
    	
  	
    	return  binutilsDir + prefix + "objdump -h " + filePath;
    }
    
    
    /**
     * Run objdump on the given binary data for the given platform
     * @param platform
     * @param binary
     * @return
     */
	public static DisassemblyOutput dis(PlatformDescriptor platform, String filePath, int offset, int length)
	{
		DisassemblyOutput output = new DisassemblyOutput();
		String listing = "";  
		listing = exec(buildDisExecStr(platform, filePath));
		if (listing.length() == 0)
		{
			return output;
		}
		
		return dis2html(listing, offset, length);
	}
	
	public static String getSections(String filePath,PlatformDescriptor platform)
	{
		String listing = "";  
		listing = exec(buildSectionExecStr(platform, filePath));
		
    	// ignore leading text
    	Pattern pattern = Pattern.compile("^\\s*[0-9a-fA-F]+:.*", Pattern.DOTALL | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(listing);
        matcher.find();
        listing = matcher.group();

		return "<pre>" + listing + "</pre>";
	}
	
}