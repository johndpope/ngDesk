package com.ngdesk.graphql.campaigns.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

public class ColumnSettings {

	@Field("ID")
	private String id;

	@Field("TEXT")
	private String text;

	@Field("TEXT_COLOR")
	private String textColor;

	@Field("FONT_FAMILY")
	private String fontFamily;

	@Field("FONT_SIZE")
	private Integer fontSize;

	@Field("FONT_WEIGHT")
	private String fontWeight;

	@Field("FONT_UNDER_LINE")
	private String fontUnderline;

	@Field("FONT_ITALICS")
	private String fontItalics;

	@Field("FONT_STYLING")
	private List<String> fontStyling;

	@Field("BACKGROUND_COLOR")
	private String backgroundColor;

	@Field("ALIGNMENT")
	private String alignment;

	@Field("HAS_FULL_WIDTH")
	private Boolean hasFullWidth;

	@Field("LINK_TO")
	private String linkTo;

	@Field("LINK_VALUE")
	private String linkValue;

	@Field("CORNER_RADIUS")
	private Integer cornerRadius;

	@Field("HAS_BORDER")
	private Boolean hasBorder;

	@Field("BORDER_WIDTH")
	private Integer borderWidth;

	@Field("BORDER_COLOR")
	private String borderColor;

	@Field("FILE")
	private String file;

	@Field("FILE_NAME")
	private String fileName;

	@Field("WIDTH")
	private Integer width;

	@Field("HEIGHT")
	private Integer height;

	@Field("ALTERNATE_TEXT")
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
