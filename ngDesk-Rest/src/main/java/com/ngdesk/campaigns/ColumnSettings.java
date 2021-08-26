package com.ngdesk.campaigns;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ColumnSettings {
	
	@JsonProperty("ID")
	private String id;
	
	@JsonProperty("TEXT")
	private String text;
	
	@JsonProperty("TEXT_COLOR")
	private String textColor;
	
	@JsonProperty("FONT_FAMILY")
	private String fontFamily;
	
	@JsonProperty("FONT_SIZE")
	private Integer fontSize;
	
	@JsonProperty("FONT_WEIGHT")
	private String fontWeight;
	
	@JsonProperty("FONT_UNDERLINE")
	private String fontUnderline;
	
	@JsonProperty("FONT_ITALICS")
	private String fontItalics;
	
	@JsonProperty("FONT_STYLING")
	private List<String> fontStyling;
	
	@JsonProperty("BACKGROUND_COLOR")
	private String backgroundColor;
	
	@JsonProperty("ALIGNMENT")
	private String alignment;
	
	@JsonProperty("HAS_FULL_WIDTH")
	private Boolean hasFullWidth;
	
	@JsonProperty("LINK_TO")
	private String linkTo;
	
	@JsonProperty("LINK_VALUE")
	private String linkValue;
	
	@JsonProperty("CORNER_RADIUS")
	private Integer cornerRadius;
	
	@JsonProperty("HAS_BORDER")
	private Boolean hasBorder;
	
	@JsonProperty("BORDER_WIDTH")
	private Integer borderWidth;
	
	@JsonProperty("BORDER_COLOR")
	private String borderColor;
	
	@JsonProperty("FILE")
	private String file;
	
	@JsonProperty("FILENAME")
	private String fileName;
	
	@JsonProperty("WIDTH")
	private Integer width;
	
	@JsonProperty("HEIGHT")
	private Integer height;
	
	@JsonProperty("ALTERNATE_TEXT")
	private String alternateText;

	public ColumnSettings() {
	}

	public ColumnSettings(String id, String text, String textColor, String fontFamily, Integer fontSize,
			String fontWeight, String fontUnderline, String fontItalics, List<String> fontStyling,
			String backgroundColor, String alignment, Boolean hasFullWidth, String linkTo, String linkValue,
			Integer cornerRadius, Boolean hasBorder, Integer borderWidth, String borderColor, String file,
			String fileName, Integer width, Integer height, String alternateText) {
		super();
		this.id = id;
		this.text = text;
		this.textColor = textColor;
		this.fontFamily = fontFamily;
		this.fontSize = fontSize;
		this.fontWeight = fontWeight;
		this.fontUnderline = fontUnderline;
		this.fontItalics = fontItalics;
		this.fontStyling = fontStyling;
		this.backgroundColor = backgroundColor;
		this.alignment = alignment;
		this.hasFullWidth = hasFullWidth;
		this.linkTo = linkTo;
		this.linkValue = linkValue;
		this.cornerRadius = cornerRadius;
		this.hasBorder = hasBorder;
		this.borderWidth = borderWidth;
		this.borderColor = borderColor;
		this.file = file;
		this.fileName = fileName;
		this.width = width;
		this.height = height;
		this.alternateText = alternateText;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTextColor() {
		return textColor;
	}

	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}

	public String getFontFamily() {
		return fontFamily;
	}

	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}

	public Integer getFontSize() {
		return fontSize;
	}

	public void setFontSize(Integer fontSize) {
		this.fontSize = fontSize;
	}

	public String getFontWeight() {
		return fontWeight;
	}

	public void setFontWeight(String fontWeight) {
		this.fontWeight = fontWeight;
	}

	public String getFontUnderline() {
		return fontUnderline;
	}

	public void setFontUnderline(String fontUnderline) {
		this.fontUnderline = fontUnderline;
	}

	public String getFontItalics() {
		return fontItalics;
	}

	public void setFontItalics(String fontItalics) {
		this.fontItalics = fontItalics;
	}

	public List<String> getFontStyling() {
		return fontStyling;
	}

	public void setFontStyling(List<String> fontStyling) {
		this.fontStyling = fontStyling;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getAlignment() {
		return alignment;
	}

	public void setAlignment(String alignment) {
		this.alignment = alignment;
	}

	public Boolean getHasFullWidth() {
		return hasFullWidth;
	}

	public void setHasFullWidth(Boolean hasFullWidth) {
		this.hasFullWidth = hasFullWidth;
	}

	public String getLinkTo() {
		return linkTo;
	}

	public void setLinkTo(String linkTo) {
		this.linkTo = linkTo;
	}

	public String getLinkValue() {
		return linkValue;
	}

	public void setLinkValue(String linkValue) {
		this.linkValue = linkValue;
	}

	public Integer getCornerRadius() {
		return cornerRadius;
	}

	public void setCornerRadius(Integer cornerRadius) {
		this.cornerRadius = cornerRadius;
	}

	public Boolean getHasBorder() {
		return hasBorder;
	}

	public void setHasBorder(Boolean hasBorder) {
		this.hasBorder = hasBorder;
	}

	public Integer getBorderWidth() {
		return borderWidth;
	}

	public void setBorderWidth(Integer borderWidth) {
		this.borderWidth = borderWidth;
	}

	public String getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(String borderColor) {
		this.borderColor = borderColor;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public String getAlternateText() {
		return alternateText;
	}

	public void setAlternateText(String alternateText) {
		this.alternateText = alternateText;
	}

	
}

