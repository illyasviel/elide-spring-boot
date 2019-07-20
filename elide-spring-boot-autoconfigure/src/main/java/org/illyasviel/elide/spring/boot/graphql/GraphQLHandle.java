/*
 * Copyright (c) 2018 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.illyasviel.elide.spring.boot.graphql;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideResponse;
import com.yahoo.elide.ElideSettings;
import com.yahoo.elide.core.DataStoreTransaction;
import com.yahoo.elide.core.ErrorObjects;
import com.yahoo.elide.core.HttpStatus;
import com.yahoo.elide.core.exceptions.CustomErrorException;
import com.yahoo.elide.core.exceptions.HttpStatusException;
import com.yahoo.elide.core.exceptions.InvalidEntityBodyException;
import com.yahoo.elide.core.exceptions.TransactionException;
import com.yahoo.elide.graphql.GraphQLRequestScope;
import com.yahoo.elide.graphql.ModelBuilder;
import com.yahoo.elide.graphql.PersistentResourceFetcher;
import com.yahoo.elide.security.User;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;

/**
 * GraphQLHandle.
 * <br>
 * Based on <a href=
 * "https://github.com/yahoo/elide/blob/master/elide-graphql/src/main/java/com/yahoo/elide/graphql/GraphQLEndpoint.java">GraphQLEndpoint.java</a>
 * Latest commit is 9d6e11efa0ba26f81286c9b0a3b298467359900a.
 * <ul>
 * <li>SecurityContext -&gt; Object(opaqueUser)</li>
 * <li>javax.ws.rs.core.Response -&gt; ElideResponse</li>
 * </ul>
 *
 * @author olOwOlo
 */
public class GraphQLHandle {

	private static final Logger log = LoggerFactory.getLogger(GraphQLHandle.class);

	private Elide elide;
	private ElideSettings elideSettings;
	private GraphQL api;

	private static final String QUERY = "query";
	private static final String OPERATION_NAME = "operationName";
	private static final String VARIABLES = "variables";
	private static final String MUTATION = "mutation";

	public GraphQLHandle(Elide elide) {
		log.info("Elide GraphQL Started ~~");
		this.elide = elide;
		this.elideSettings = elide.getElideSettings();
		PersistentResourceFetcher fetcher = new PersistentResourceFetcher(elide.getElideSettings());
		ModelBuilder builder = new ModelBuilder(elide.getElideSettings().getDictionary(), fetcher);
		this.api = GraphQL.newGraphQL(builder.build()).build();
	}

	/**
	 * Create handler.
	 *
	 * @param graphQLDocument post data as jsonapi document
	 * @param opaqueUser opaqueUser
	 * @return response
	 */
	public ElideResponse post(String graphQLDocument, Object opaqueUser) {
		ObjectMapper mapper = elide.getMapper().getObjectMapper();

		JsonNode topLevel;

		try {
			topLevel = mapper.readTree(graphQLDocument);
		} catch (IOException e) {
			log.debug("Invalid json body provided to GraphQL", e);
			// NOTE: Can't get at isVerbose setting here for hardcoding to false. If necessary, we can refactor
			// so this can be set appropriately.
			return buildErrorResponse(new InvalidEntityBodyException(graphQLDocument), false);
		}

		Function<JsonNode, ElideResponse> executeRequest = (node) -> executeGraphQLRequest(mapper, opaqueUser,
				graphQLDocument, node);

		if (topLevel.isArray()) {
			Iterator<JsonNode> nodeIterator = topLevel.iterator();
			Iterable<JsonNode> nodeIterable = () -> nodeIterator;
			// NOTE: Create a non-parallel stream
			// It's unclear whether or not the expectations of the caller would be that requests are intended
			// to run serially even outside of a single transaction. We should revisit this.
			Stream<JsonNode> nodeStream = StreamSupport.stream(nodeIterable.spliterator(), false);
			ArrayNode result = nodeStream.map(executeRequest).map(response -> {
				try {
					return mapper.readTree(response.getBody());
				} catch (IOException e) {
					log.debug("Caught an IO exception while trying to read response body");
					return JsonNodeFactory.instance.objectNode();
				}
			}).reduce(JsonNodeFactory.instance.arrayNode(), (arrayNode, node) -> arrayNode.add(node),
					(left, right) -> left.addAll(right));
			try {
				return new ElideResponse(HttpStatus.SC_OK, mapper.writeValueAsString(result));
			} catch (JsonProcessingException e) {
				log.error("An unexpected error occurred trying to serialize array response.", e);
				throw new RuntimeException(e); // 500
			}
		}

		return executeRequest.apply(topLevel);
	}

	private ElideResponse executeGraphQLRequest(ObjectMapper mapper, Object opaqueUser, String graphQLDocument,
			JsonNode jsonDocument) {
		boolean isVerbose = false;
		try (DataStoreTransaction tx = elide.getDataStore().beginTransaction()) {
			final User user = tx.accessUser(opaqueUser);
			GraphQLRequestScope requestScope = new GraphQLRequestScope(tx, user, elide.getElideSettings());
			isVerbose = requestScope.getPermissionExecutor().isVerbose();

			if (!jsonDocument.has(QUERY)) {
				return new ElideResponse(HttpStatus.SC_BAD_REQUEST, "A `query` key is required.");
			}

			String query = jsonDocument.get(QUERY).asText();

			// Logging all queries. It is recommended to put any private information that shouldn't be logged into
			// the "variables" section of your query. Variable values are not logged.
			log.info("Processing GraphQL query:\n{}", query);

			ExecutionInput.Builder executionInput = new ExecutionInput.Builder().context(requestScope).query(query);

			if (jsonDocument.has(OPERATION_NAME) && !jsonDocument.get(OPERATION_NAME).isNull()) {
				executionInput.operationName(jsonDocument.get(OPERATION_NAME).asText());
			}

			if (jsonDocument.has(VARIABLES) && !jsonDocument.get(VARIABLES).isNull()) {
				Map<String, Object> variables = mapper.convertValue(jsonDocument.get(VARIABLES), Map.class);
				executionInput.variables(variables);
			}

			ExecutionResult result = api.execute(executionInput);

			tx.preCommit();
			requestScope.runQueuedPreSecurityTriggers();
			requestScope.getPermissionExecutor().executeCommitChecks();
			if (query.trim().startsWith(MUTATION)) {
				if (!result.getErrors().isEmpty()) {
					HashMap<String, Object> abortedResponseObject = new HashMap<>();
					abortedResponseObject.put("errors", result.getErrors());
					abortedResponseObject.put("data", null);

					// Do not commit. Throw OK response to process tx.close correctly.

					throw new GraphQLErrorException(200, mapper.writeValueAsString(abortedResponseObject));
				}
				requestScope.saveOrCreateObjects();
			}
			tx.flush(requestScope);

			requestScope.runQueuedPreCommitTriggers();
			elide.getAuditLogger().commit(requestScope);
			tx.commit(requestScope);
			requestScope.runQueuedPostCommitTriggers();

			if (log.isTraceEnabled()) {
				requestScope.getPermissionExecutor().printCheckStats();
			}

			return new ElideResponse(HttpStatus.SC_OK, mapper.writeValueAsString(result.toSpecification()));
		} catch (WebApplicationException e) {
			log.debug("WebApplicationException", e);
			throw e;
		} catch (GraphQLErrorException e) {
			log.debug("GraphQLErrorException", e);
			return e.getResponse();
		} catch (JsonProcessingException e) {
			log.debug("Invalid json body provided to GraphQL", e);
			return buildErrorResponse(new InvalidEntityBodyException(graphQLDocument), isVerbose);
		} catch (IOException e) {
			log.error("Uncaught IO Exception by Elide in GraphQL", e);
			return buildErrorResponse(new TransactionException(e), isVerbose);
		} catch (HttpStatusException e) {
			log.debug("Caught HTTP status exception {}", e.getStatus(), e);
			return buildErrorResponse(new HttpStatusException(200, "") {
				private static final long serialVersionUID = 1L;

				@Override
				public int getStatus() {
					return 200;
				}

				@Override
				public Pair<Integer, JsonNode> getErrorResponse() {
					return e.getErrorResponse();
				}

				@Override
				public Pair<Integer, JsonNode> getVerboseErrorResponse() {
					return e.getVerboseErrorResponse();
				}

				@Override
				public String getVerboseMessage() {
					return e.getVerboseMessage();
				}

				@Override
				public String toString() {
					return e.toString();
				}
			}, isVerbose);
		} catch (Exception | Error e) {
			log.debug("Unhandled error or exception.", e);
			throw e;
		} finally {
			elide.getAuditLogger().clear();
		}
	}

	private ElideResponse buildErrorResponse(HttpStatusException error, boolean isVerbose) {
		ObjectMapper mapper = elide.getMapper().getObjectMapper();
		JsonNode errorNode;
		if (!(error instanceof CustomErrorException) && elideSettings.isReturnErrorObjects()) {
			ErrorObjects errors = ErrorObjects.builder().addError()
					.with("message", isVerbose ? error.getVerboseMessage() : error.toString()).build();
			errorNode = mapper.convertValue(errors, JsonNode.class);
		} else {
			errorNode = isVerbose ? error.getVerboseErrorResponse().getRight() : error.getErrorResponse().getRight();
		}
		String errorBody;
		try {
			errorBody = mapper.writeValueAsString(errorNode);
		} catch (JsonProcessingException e) {
			errorBody = errorNode.toString();
		}
		return new ElideResponse(error.getStatus(), errorBody);
	}
}
