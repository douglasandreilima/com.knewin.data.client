package com.knewin.data.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class RestRequest {

	private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(30);

	private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofMinutes(3);


	/**
	 * Request content using HTTP GET method.
	 *
	 * @param url the URL
	 *
	 * @return the response content
	 *
	 * @throws DataRequestException error in case of problems to request remote content
	 *
	 * @deprecated Uses
	 *             {@link DataRequest#request(com.knewin.data.client.info.DataRequestInfo, String)}
	 */
	@Deprecated
	public String get(final String url) throws DataRequestException {
		final RequestConfig requestConfig = RequestConfig.custom()
			.setConnectionRequestTimeout((int) DEFAULT_CONNECT_TIMEOUT.toMillis())
			.setSocketTimeout((int) DEFAULT_READ_TIMEOUT.toMillis())
			.setConnectTimeout((int) DEFAULT_CONNECT_TIMEOUT.toMillis()).build();

		try (final CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig)
			.build()) {
			return this.get(url, httpClient);
		} catch (final DataRequestException e) {
			e.setRequest(url);
			throw e;
		} catch (final Exception e) {
			throw new DataRequestException(e, url);
		}
	}


	/**
	 * Request content using HTTP GET method.
	 *
	 * @param url the URL
	 * @param httpClient a {@link CloseableHttpClient} instance
	 *
	 * @return the response content
	 *
	 * @throws DataRequestException error in case of problems to request remote content
	 *
	 * @deprecated Uses
	 *             {@link DataRequest#request(com.knewin.data.client.info.DataRequestInfo, String, CloseableHttpClient)}
	 */
	@Deprecated
	public String get(final String url, final CloseableHttpClient httpClient) throws DataRequestException {
		try (final CloseableHttpResponse httpResponse = httpClient.execute(new HttpGet(url))) {
			return handleResponse(httpResponse);
		} catch (final DataRequestException e) {
			e.setRequest(url);
			throw e;
		} catch (final Exception e) {
			throw new DataRequestException(e, url);
		}
	}


	/**
	 * Request content using HTTP POST method.
	 *
	 * @param bodyContent the body content that contains the parameters request
	 * @param url the URL
	 *
	 * @return the response content
	 *
	 * @throws DataRequestException error in case of problems to request remote content
	 */
	protected String post(final String bodyContent, final String url) throws DataRequestException {
		final RequestConfig requestConfig = RequestConfig.custom()
			.setConnectionRequestTimeout((int) DEFAULT_CONNECT_TIMEOUT.toMillis())
			.setSocketTimeout((int) DEFAULT_READ_TIMEOUT.toMillis())
			.setConnectTimeout((int) DEFAULT_READ_TIMEOUT.toMillis()).build();

		try (final CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig)
			.build()) {
			return this.post(bodyContent, url, httpClient);
		} catch (final DataRequestException e) {
			e.setRequest(bodyContent);
			throw e;
		} catch (final Exception e) {
			throw new DataRequestException(e, bodyContent);
		}
	}


	/**
	 * Request content using HTTP POST method.
	 *
	 * @param bodyContent the body content that contains the parameters request
	 * @param url the URL
	 * @param httpClient a {@link CloseableHttpClient} instance
	 *
	 * @return the response content
	 *
	 * @throws DataRequestException error in case of problems to request remote content
	 */
	protected String post(final String postContent, final String url, final CloseableHttpClient httpClient)
		throws DataRequestException {
		final HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(new StringEntity(postContent, StandardCharsets.UTF_8));

		try (final CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
			return handleResponse(httpResponse);
		} catch (final DataRequestException e) {
			e.setRequest(postContent);
			throw e;
		} catch (final Exception e) {
			throw new DataRequestException(e, postContent);
		}
	}


	private String handleResponse(final CloseableHttpResponse httpResponse) throws IOException, DataRequestException {
		final HttpEntity entity = httpResponse.getEntity();
		final String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
		if (httpResponse.getStatusLine().getStatusCode() < 400) {
			return responseBody;
		}
		throw new DataRequestException(httpResponse.getStatusLine().getReasonPhrase() + "\nResponse body: " + responseBody);
	}
}
