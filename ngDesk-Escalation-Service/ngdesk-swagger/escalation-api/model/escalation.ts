/**
 * OpenAPI definition
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: v0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { EscalationRule } from './escalationRule';


export interface Escalation { 
    /**
     * Autogenerated Id
     */
    readonly ESCALATION_ID?: string;
    /**
     * Name of the escalation
     */
    NAME: string;
    /**
     * Description of the escalation
     */
    DESCRIPTION?: string;
    /**
     * Escalation Rules
     */
    RULES: Array<EscalationRule>;
    /**
     * Date Created
     */
    readonly DATE_CREATED?: string;
    /**
     * Date Updated
     */
    readonly DATE_UPDATED?: string;
    /**
     * Last Updated By
     */
    readonly LAST_UPDATED_BY?: string;
    /**
     * Created By
     */
    readonly CREATED_BY?: string;
}

