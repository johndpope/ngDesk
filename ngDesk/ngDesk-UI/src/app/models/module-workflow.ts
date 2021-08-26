import { Condition } from './condition';

export class Values {

  constructor(private BODY?: any) { }

  public get body() {
    return this.BODY;
  }

  public set body(body: any) {
    this.BODY = body;
  }

}



export class ConnectionsTo {
  constructor(
    private TITLE: string,
    private FROM: string,
    private TO_NODE: string
  ) { }

  public get title() {
    return this.TITLE;
  }

  public set title(title: string) {
    this.TITLE = title;
  }

  public get from() {
    return this.FROM;
  }

  public set from(from: string) {
    this.FROM = from;
  }

  public get toNode() {
    return this.TO_NODE;
  }

  public set toNode(toNode: string) {
    this.TO_NODE = toNode;
  }
}

export class Plug {
  constructor(
    private ORDER: number,
    private ID: string,
    private NAME: string
  ) { }

  public get order() {
    return this.ORDER;
  }

  public set order(order: number) {
    this.ORDER = order;
  }

  public get id() {
    return this.ID;
  }

  public set id(id: string) {
    this.ID = id;
  }

  public get name() {
    return this.NAME;
  }

  public set name(name: string) {
    this.NAME = name;
  }
}

export class Node {
  constructor(
    private POSITION_X: string,
    private POSITION_Y: string,
    private VALUES: Values,
    private ID: string,
    private TYPE: string,
    private CONNECTIONS_TO: ConnectionsTo[],
    private PLUGS: Plug[],
    private NAME: string
  ) { }

  public get positionX() {
    return this.POSITION_X;
  }

  public set positionX(positionX: string) {
    this.POSITION_X = positionX;
  }

  public get positionY() {
    return this.POSITION_Y;
  }

  public set positionY(positionY: string) {
    this.POSITION_Y = positionY;
  }

  public get values() {
    return this.VALUES;
  }

  public set values(values: Values) {
    this.VALUES = values;
  }

  public get id() {
    return this.ID;
  }

  public set id(id: string) {
    this.ID = id;
  }

  public get type() {
    return this.TYPE;
  }

  public set type(type: string) {
    this.TYPE = type;
  }

  public get connectionsTo() {
    return this.CONNECTIONS_TO;
  }

  public set connectionsTo(connectionsTo: ConnectionsTo[]) {
    this.CONNECTIONS_TO = connectionsTo;
  }

  public get plugs() {
    return this.PLUGS;
  }

  public set plugs(plugs: Plug[]) {
    this.PLUGS = plugs;
  }

  public get name() {
    return this.NAME;
  }

  public set name(name: string) {
    this.NAME = name;
  }
}

export class Workflow {
  constructor(
    private NODES: Node[],
    private LAST_UPDATED_BY?: any,
    private DATE_UPDATED?: any
  ) { }

  public get nodes() {
    return this.NODES;
  }

  public set nodes(nodes: Node[]) {
    this.NODES = nodes;
  }

  public get lastUpdatedBy() {
    return this.LAST_UPDATED_BY;
  }

  public set lastUpdatedBy(lastUpdatedBy: any) {
    this.LAST_UPDATED_BY = lastUpdatedBy;
  }

  public get dateUpdated() {
    return this.DATE_UPDATED;
  }

  public set dateUpdated(dateUpdated: any) {
    this.DATE_UPDATED = dateUpdated;
  }
}

export class ModuleWorkflow {
  constructor(
    private NAME: string,
    private DESCRIPTION: string,
    private CONDITIONS: Condition[],
    private TYPE: {
      DISPLAY: string;
      BACKEND: string;
    },
    private WORKFLOW: Workflow,
    private WORKFLOW_ID?: string,
    private DATE_CREATED?: any,
    private LAST_UPDATED_BY?: any,
    private DATE_UPDATED?: any,
    private ORDER?: any
  ) { }

  public get name() {
    return this.NAME;
  }

  public set name(name: string) {
    this.NAME = name;
  }

  public get description() {
    return this.DESCRIPTION;
  }

  public set description(description: string) {
    this.DESCRIPTION = description;
  }

  public get conditions() {
    return this.CONDITIONS;
  }

  public set conditions(conditions: Condition[]) {
    this.CONDITIONS = conditions;
  }

  public get type() {
    return this.TYPE;
  }

  public set type(type: { DISPLAY: string; BACKEND: string }) {
    this.TYPE = type;
  }

  public get workflow() {
    return this.WORKFLOW;
  }

  public set workflow(workflow: Workflow) {
    this.WORKFLOW = workflow;
  }

  public get workflowId() {
    return this.WORKFLOW_ID;
  }

  public set workflowId(workflowId: string) {
    this.WORKFLOW_ID = workflowId;
  }

  public get dateCreated() {
    return this.DATE_CREATED;
  }

  public set dateCreated(dateCreated: any) {
    this.DATE_CREATED = dateCreated;
  }

  public get lastUpdatedBy() {
    return this.LAST_UPDATED_BY;
  }

  public set lastUpdatedBy(lastUpdatedBy: any) {
    this.LAST_UPDATED_BY = lastUpdatedBy;
  }

  public get dateUpdated() {
    return this.DATE_UPDATED;
  }

  public set dateUpdated(dateUpdated: any) {
    this.DATE_UPDATED = dateUpdated;
  }
  public get order() {
    return this.ORDER;
  }

  public set order(order: any) {
    this.ORDER = order;
  }
}
