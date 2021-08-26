package com.ngdesk.nodes;

import java.util.Map;

import org.bson.Document;

public abstract class Node {

	public abstract Map<String, Object> executeNode(Document node, Map<String, Object> inputMessage);
}
