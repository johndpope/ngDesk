package com.ngdesk.graphql.utilities;

import static graphql.GraphQL.newGraphQL;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import com.ngdesk.graphql.categories.dao.CategoriesNoAuthCountDataFetcher;
import com.ngdesk.graphql.categories.dao.CategoriesNoAuthDataFetcher;
import com.ngdesk.graphql.categories.dao.CategoryNoAuthDataFetcher;
import com.ngdesk.graphql.datatypes.DateTime;
import com.ngdesk.graphql.knowledgebase.article.dao.ArticleNoAuthDataFetcher;
import com.ngdesk.graphql.knowledgebase.article.dao.ArticlesBySectionIdNoAuthDataFetcher;
import com.ngdesk.graphql.knowledgebase.article.dao.ArticlesNoAuthCountFetcher;
import com.ngdesk.graphql.knowledgebase.article.dao.ArticlesNoAuthDataFetcher;
import com.ngdesk.graphql.knowledgebase.section.dao.SectionNoAuthCategoryDataFetcher;
import com.ngdesk.graphql.knowledgebase.section.dao.SectionNoAuthDataFetcher;
import com.ngdesk.graphql.knowledgebase.section.dao.SectionsNoAuthCountFetcher;
import com.ngdesk.graphql.knowledgebase.section.dao.SectionsNoAuthDataFetcher;
import com.ngdesk.graphql.modules.data.dao.EntryDataFetcher;
import com.ngdesk.graphql.userplugin.dao.AllPublishedUserPluginsDataFetcher;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.RuntimeWiring.Builder;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.micrometer.core.instrument.util.IOUtils;

@Component
public class DataUtilityForNoAuthCalls {

	@Value("classpath:company-no-auth-calls-schema.graphqls")
	private Resource companyNoAuthCallsSchemaResource;

	@Autowired
	AllPublishedUserPluginsDataFetcher allPublishedUserPluginsDataFetcher;

	@Autowired
	DateTime dateTime;

	@Autowired
	EntryDataFetcher entryDataFetcher;

	@Autowired
	SectionsNoAuthDataFetcher sectionsNoAuthDataFetcher;

	@Autowired

	SectionsNoAuthCountFetcher sectionsNoAuthCountFetcher;

	@Autowired
	SectionNoAuthCategoryDataFetcher sectionNoAuthCategoryDataFetcher;

	@Autowired
	SectionNoAuthDataFetcher sectionNoAuthDataFetcher;

	@Autowired
	CategoryNoAuthDataFetcher categoryNoAuthDataFetcher;

	@Autowired
	CategoriesNoAuthDataFetcher categoriesNoAuthDataFetcher;

	@Autowired
	CategoriesNoAuthCountDataFetcher categoriesNoAuthCountDataFetcher;

	@Autowired
	ArticleNoAuthDataFetcher articleNoAuthDataFetcher;

	@Autowired
	ArticlesNoAuthDataFetcher articlesNoAuthDataFetcher;

	@Autowired
	ArticlesNoAuthCountFetcher articlesNoAuthCountFetcher;

	@Autowired
	ArticlesBySectionIdNoAuthDataFetcher articlesBySectionIdNoAuthDataFetcher;

	public GraphQL createGraphQlObject() throws IOException {

		try {
			String schemaString = IOUtils.toString(companyNoAuthCallsSchemaResource.getInputStream());
			TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schemaString);
			RuntimeWiring wiring = buildRuntimeWiring();
			GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
			return newGraphQL(schema).build();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public RuntimeWiring buildRuntimeWiring() {
		Builder builder = RuntimeWiring.newRuntimeWiring();
		builder.scalar(dateTime.dateScalar);

		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getPublishedUserPlugins", allPublishedUserPluginsDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getKbSection", sectionNoAuthDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getKbSections", sectionsNoAuthDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getKbSectionsCount", sectionsNoAuthCountFetcher));
		builder.type("Section", typeWiring -> typeWiring.dataFetcher("category", sectionNoAuthCategoryDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getKbCategory", categoryNoAuthDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getKbCategories", categoriesNoAuthDataFetcher));
		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getCategoriesCount", categoriesNoAuthCountDataFetcher));
		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getKbCountCategories", categoriesNoAuthCountDataFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getKbArticle", articleNoAuthDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getAllKbArticles", articlesNoAuthDataFetcher));
		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getAllKbArticlesCount", articlesNoAuthCountFetcher));
		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getArticlesBySectionId", articlesBySectionIdNoAuthDataFetcher));

		// builder.type("Category", typeWiring -> typeWiring.dataFetcher("createdBy",
		// entryDataFetcher));
//		builder.type("Category", typeWiring -> typeWiring.dataFetcher("lastUpdatedBy", entryDataFetcher));

		return builder.build();

	}

}
