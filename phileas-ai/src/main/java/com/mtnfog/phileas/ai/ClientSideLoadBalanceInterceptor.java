package com.mtnfog.phileas.ai;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Interceptor that enables client-side load balancing.
 */
public class ClientSideLoadBalanceInterceptor implements Interceptor {

    private static final Logger LOGGER = LogManager.getLogger(ClientSideLoadBalanceInterceptor.class);

    private List<HttpUrl> hosts;

    /**
     * To enable client-side load balancing provide a list of endpoints. An endpoint will be
     * selected from the list at random by the interceptor.
     * @param endpoints A list of endpoints.
     * @throws MalformedURLException Thrown if an URL is invalid.
     */
    public ClientSideLoadBalanceInterceptor(List<String> endpoints) throws MalformedURLException {

        this.hosts = new LinkedList<>();

        for(String host: endpoints) {

            final URL url = new URL(host);

            final HttpUrl newUrl = HttpUrl.get(url);

            this.hosts.add(newUrl);

        }

    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        final HttpUrl newUrl = hosts.stream()
                .skip((int) (hosts.size() * Math.random()))
                .findFirst().get();

        LOGGER.debug("Using philter-ner host {}", newUrl.toString());

        final Request request = chain.request().newBuilder()
                .url(newUrl)
                .build();

        return chain.proceed(request);

    }

}
