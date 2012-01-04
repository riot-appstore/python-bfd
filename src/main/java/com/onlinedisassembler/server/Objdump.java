package com.onlinedisassembler.server;

import java.io.*;
import java.util.regex.*;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import com.onlinedisassembler.shared.ObjectType;
import com.onlinedisassembler.shared.PlatformDescriptor;


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
    
    private static String buildDisExecStr(PlatformDescriptor platform, String filePath, ObjectType objType)
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
    	
    	String targetTypeStr = "";
    	switch ( objType )
    	{
    		case ELF:
//    			if ( platform.endian == Endian.BIG )
//    			{
//    				targetTypeStr = " -b elf32-big ";
//    			}
//    			else
//    			{
//    				targetTypeStr = " -b elf32-little ";
//    			}
//    			break;
    			
    		case PE: 
    		case BINARY:
    			targetTypeStr = " -b binary ";
    			
    	}
    	
    	return  binutilsDir + prefix + "objdump -D" + targetTypeStr + "-w -z -m " + machine + " --adjust-vma=" + platform.baseAddress + endian + " " + option + " " + filePath;
    }

    
    private static String buildSectionExecStr(PlatformDescriptor platform, String filePath)
    {

    	String prefix = getPrefix(platform);
		String binutilsDir = HostUtils.getBinutilsDir();    		
    	
  	
    	return  binutilsDir + prefix + "objdump -w -h " + filePath;
    }
    
    
    /**
     * Run objdump on the given binary data for the given platform
     * @param platform
     * @param binary
     * @return
     */
	public static String dis(PlatformDescriptor platform, String filePath, ObjectType objType)
	{
		String listing = "";  
		listing = exec(buildDisExecStr(platform, filePath, objType));		
		return listing;
	}
	
	public static String getSections(String filePath,PlatformDescriptor platform)
	{
		String listing = "";  
		listing = exec(buildSectionExecStr(platform, filePath));
		

    	// ignore leading text
//    	Pattern pattern = Pattern.compile("^\\s*[0-9a-fA-F]+:.*", Pattern.DOTALL | Pattern.MULTILINE);
//        Matcher matcher = pattern.matcher(listing);
//        matcher.find();
//        listing = matcher.group();

		return listing;
		//return "<pre>&#9472;&#9472;\n&#9492;&#9492;&#9472;</pre>";
	}
	
}