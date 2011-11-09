package com.reverbin.ODA.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.event.dom.client.*;
import com.reverbin.ODA.shared.*;

public class ViewPlatformSelection extends FlowPanel 
		implements ChangeHandler, ModelPlatformBinListener, ClickHandler {

	ModelPlatformBin modelPlatformBin;
	ListBox listBoxPlatform = new ListBox();
	ListBox listBoxEndian = new ListBox();
	ListBox listBoxOption = new ListBox();
	Label labelListBoxOption = new Label("Option");
    TextBox baseAdressText = new TextBox();
    Button disButton = new Button("Disassemble");	
	
	public ViewPlatformSelection(ModelPlatformBin mpb)
	{
		modelPlatformBin = mpb;
		mpb.addListener(this);
	}
	
	protected void onLoad()
	{
        // disassembly tab
        VerticalPanel asmHeaderVp = new VerticalPanel();
        asmHeaderVp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        
        for (Platform platform : Platform.getSupportedPlatforms())
        {
        	listBoxPlatform.addItem(platform.getName());
        }
        listBoxPlatform.setVisibleItemCount(1);
        listBoxPlatform.addChangeHandler(this);
        
        listBoxEndian.setVisibleItemCount(1);
        
        listBoxOption.setVisibleItemCount(1);
        listBoxOption.setVisible(false);
        labelListBoxOption.setVisible(false);
        
        HorizontalPanel firstRow = new HorizontalPanel();   
        firstRow.setSpacing(3);
        firstRow.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        firstRow.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        firstRow.add(new Label("Platform"));
        firstRow.add(listBoxPlatform);
        firstRow.add(new Label("     "));
        firstRow.add(new Label("Endian"));
        firstRow.add(listBoxEndian);
        firstRow.add(new Label("     "));
        firstRow.add(labelListBoxOption);
        firstRow.add(listBoxOption);

        
        HorizontalPanel secondRow = new HorizontalPanel();
        secondRow.setSpacing(3);
        secondRow.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        secondRow.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        secondRow.add(new Label("Base Address"));

        baseAdressText.setVisibleLength(8);
        secondRow.add(baseAdressText);
        
        disButton.addClickHandler(this);
        
        asmHeaderVp.add(firstRow);
        asmHeaderVp.add(secondRow);
        asmHeaderVp.add(disButton);
        //asmHeaderVp.add(thirdRowButton);
        
        this.add(asmHeaderVp);
        this.setStyleName("panelBox");
        
        // set default platform
        PlatformDescriptor platform = new PlatformDescriptor();
        platform.baseAddress = 0;
        platform.platformId = PlatformId.X86;
        platform.endian = Endian.DEFAULT;
        updateView(platform);
	}
	
	private void updateView(PlatformDescriptor platformDesc)
	{
		Platform p = Platform.getPlatform(platformDesc.platformId);
		for (int i = 0; i < listBoxPlatform.getItemCount(); i++) {
			if (p.getName().equals(listBoxPlatform.getItemText(i))) {
				listBoxPlatform.setSelectedIndex(i);
			}
		}
		
		listBoxEndian.clear();
		for (Endian e : p.getEndians()) {
			listBoxEndian.addItem(e.toString());
			if (e == platformDesc.endian) {
				listBoxEndian.setSelectedIndex(listBoxEndian.getItemCount() - 1);
			}				
		}
		
		// Only make the Option ListBox visible for those platforms with options
		listBoxOption.clear();
		if ( p.getOptions().size() != 0 )
		{
			listBoxOption.setVisible(true);
			labelListBoxOption.setVisible(true);
			for (PlatformOption opt : p.getOptions()) {
				listBoxOption.addItem(opt.toString());
				if (opt == platformDesc.option) {
					listBoxOption.setSelectedIndex(listBoxOption.getItemCount() - 1);
				}				
			}	
		}
		else			
		{
			listBoxOption.setVisible(false);
			labelListBoxOption.setVisible(false);
		}
	    
		baseAdressText.setText("0x" + Integer.toHexString(platformDesc.baseAddress));
		
	}
	
	public void onChange(ChangeEvent event)
	{
		Platform p = Platform.getPlatform(
				listBoxPlatform.getItemText(listBoxPlatform.getSelectedIndex()));
		
		listBoxEndian.clear();
		for (Endian e : p.getEndians()) {
			listBoxEndian.addItem(e.toString());
			if (Endian.DEFAULT == e)
				listBoxEndian.setSelectedIndex(listBoxEndian.getItemCount());
		}
		
		// Only make the Option ListBox visible for those platforms with options
		listBoxOption.clear();
		if ( p.getOptions().size() != 0 )
		{
			listBoxOption.setVisible(true);
			labelListBoxOption.setVisible(true);
			for (PlatformOption opt : p.getOptions()) {
				listBoxOption.addItem(opt.toString());
				
				// Default to "NONE"
				if (PlatformOption.DEFAULT == opt) {
					listBoxOption.setSelectedIndex(listBoxOption.getItemCount());
				}				
			}	
		}
		else
		{
			listBoxOption.setVisible(false);
			labelListBoxOption.setVisible(false);
		}
	
	}
	
	public void onChange(ModelPlatformBin mpb, int eventFlags)
	{
		if (0 != (eventFlags & mpb.MODEL_EVENT_PLATFORM_CHANGED)) {
			updateView(mpb.getPlatform());
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		PlatformDescriptor platformDesc = new PlatformDescriptor();
		platformDesc.platformId = Platform.getPlatform(
			listBoxPlatform.getItemText(listBoxPlatform.getSelectedIndex())).getId();
		platformDesc.endian = Endian.getEndian(
				listBoxEndian.getItemText(listBoxEndian.getSelectedIndex()));
		if ( listBoxOption.getSelectedIndex() > -1)
		{
			platformDesc.option = PlatformOption.getPlatformOption(
				listBoxOption.getItemText(listBoxOption.getSelectedIndex()));
		}
		platformDesc.baseAddress = Integer.parseInt(baseAdressText.getText().replaceFirst("0x", ""), 16);
		modelPlatformBin.setPlatform(platformDesc);	
	}
}
