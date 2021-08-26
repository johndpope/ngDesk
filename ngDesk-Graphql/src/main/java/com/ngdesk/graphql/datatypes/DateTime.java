package com.ngdesk.graphql.datatypes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

@Component
public class DateTime {

	public GraphQLScalarType dateScalar;

	@Autowired
	DateTime dateTime;

	@PostConstruct
	public void dateScalar() {
		dateTime.dateScalar = GraphQLScalarType.newScalar().name("DateTime").description("Description")
				.coercing(new Coercing<Date, String>() {

					@Override
					public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
						SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
						try {
							Date date = format.parse(dataFetcherResult.toString());
							 return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(date);
						} catch (ParseException e) {
							e.printStackTrace();
						}
						return null;
					}

					@Override
					public Date parseValue(Object input) throws CoercingParseValueException {
						System.out.println("parseValue");
						// stuff
						return null;
					}

					@Override
					public Date parseLiteral(Object input) throws CoercingParseLiteralException {
						System.out.println("parse literal");
						// stuff
						return null;
					}
				}).build();
	}
}
