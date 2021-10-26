package com.ngdesk.escalation.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.repositories.EscalationRepository;

@Component
public class BeforeSaveListener extends AbstractMongoEventListener<Escalation> {

	@Autowired
	EscalationRepository escalationRepository;

	@Override
	public void onBeforeConvert(BeforeConvertEvent<Escalation> event) {
		Escalation escalation = event.getSource();

		if (escalation.getId() == null) {
			// NEW ESCALATION
			Optional<Escalation> optional = escalationRepository.findEscalationByName(escalation.getName(),
					event.getCollectionName());
			if (optional.isPresent()) {
				String[] variables = { "ESCALATION_NAME", "NAME" };
				throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", variables);
			}

		} else {
			// EXISTING ESCALATION CHECK IF ANY OTHER ESCALATION HAS SAME NAME
			Optional<Escalation> optional = escalationRepository.findOtherEscalationsWithDuplicateName(
					escalation.getName(), escalation.getId(), event.getCollectionName());
			if (optional.isPresent()) {
				String[] variables = { "ESCALATION_NAME", "NAME" };
				throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", variables);
			}
		}

		if (!validateEscalationRuleOrder(escalation)) {
			throw new BadRequestException("ESCALATION_RULE_ORDER_INVALID", null);
		}

		if (!validateEscalationRuleMinutes(escalation)) {
			throw new BadRequestException("ESCALATION_RULE_MINS_AFTER_INVALID", null);
		}

		for (EscalationRule rule : escalation.getRules()) {
			EscalateTo escalateTo = rule.getEscalateTo();

			if (escalateTo.getSchedules() == null && escalateTo.getTeams() == null && escalateTo.getUsers() == null) {
				throw new BadRequestException("ESCALATE_TO_REQUIRED", null);
			}

			if (escalateTo.getSchedules().size() == 0 && escalateTo.getTeams().size() == 0
					&& escalateTo.getUsers().size() == 0) {
				throw new BadRequestException("ESCALATE_TO_REQUIRED", null);
			}
		}

	}

	public boolean validateEscalationRuleOrder(Escalation escalation) {
		List<EscalationRule> rules = escalation.getRules();
		for (int i = 0; i < rules.size(); ++i) {
			if (i + 1 != rules.get(i).getOrder()) {
				return false;
			}
		}
		return true;
	}

	public boolean validateEscalationRuleMinutes(Escalation escalation) {
		List<EscalationRule> rules = escalation.getRules();
		for (int i = 0; i < rules.size(); i++) {
			EscalationRule rule = rules.get(i);
			if (i != rules.size() - 1) {
				EscalationRule nextRule = rules.get(i + 1);
				if (rule.getMinsAfter() >= nextRule.getMinsAfter()) {
					return false;
				}
			}
		}
		return true;
	}

}
