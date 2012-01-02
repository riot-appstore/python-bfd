/**
 * 
 */
package com.reverbin.ODA.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;

/**
 * Container for hex view
 * 
 * @author anthony
 *
 */
public class ViewHex extends FlowPanel implements ModelPlatformBinListener, ClickHandler, SubmitCompleteHandler {

	private ModelPlatformBin modelPlatformBin;
	private TextArea textArea = new TextArea();
	Button hexEditButton = new Button("Edit");
	Button hexUploadButton = new Button("Upload File");
	UploadFile uploadFile = new UploadFile(this);
	private final int DISPLAY_CHUNK_SIZE = 2000;
	private byte[] currentBytes = new byte[]{};
	private byte[] displayBytes = new byte[DISPLAY_CHUNK_SIZE];
	
	/**
	 * Constructor
	 * @param asmView
	 */
	public ViewHex(ModelPlatformBin mb) {
		modelPlatformBin = mb;
		mb.addListener(this);
		textArea.setReadOnly(true);
	}
	
	protected void onLoad()
	{
		hexEditButton.addClickHandler(this);
		hexUploadButton.addClickHandler(this);
		
        FlowPanel hexHeaderPanel = new FlowPanel();
        HorizontalPanel firstRow = new HorizontalPanel();   
        firstRow.add(hexUploadButton);
        firstRow.add(hexEditButton);
        hexHeaderPanel.add(firstRow);
        hexHeaderPanel.setStyleName("panelBox");
        int clientHeight = Window.getClientHeight();
        textArea.setSize("574px", "" + (int) (clientHeight*2/3) + "px");
        textArea.setStyleName("textarea");

        this.add(hexHeaderPanel);
        this.add(textArea);
        this.setSize("600px", "" + (int) (clientHeight*2/3 + 82) + "px" );
	}

	@Override
	public void onChange(ModelPlatformBin mpb, int eventFlags) {
		if (0 != (eventFlags & mpb.MODEL_EVENT_BIN_CHANGED)) {
			currentBytes = mpb.getBytes();
			displayBytes = new byte[Math.min(DISPLAY_CHUNK_SIZE, currentBytes.length)];
			System.arraycopy(currentBytes, 0, displayBytes, 0, displayBytes.length);
			textArea.setText(HexUtils.bytesToText(displayBytes));
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		
		if (event.getSource().equals(hexEditButton)) {
			if (textArea.isReadOnly()) {
				textArea.setReadOnly(false);
				hexEditButton.setText("Save");
				textArea.setText(HexUtils.bytesToText(currentBytes));
				textArea.setFocus(true);
				textArea.setCursorPos(0);
			}
			else {
				modelPlatformBin.setBytes(HexUtils.textToBytes(textArea.getText()));
				textArea.setReadOnly(true);
				hexEditButton.setText("Edit");
			}
		} else {

       	 	uploadFile.center();
       	 	uploadFile.show();
		}
	}

	@Override
	public void onSubmitComplete(SubmitCompleteEvent event) {
		modelPlatformBin.setBytes(HexUtils.textToBytes(event.getResults()));
		uploadFile.hide();
	}
}