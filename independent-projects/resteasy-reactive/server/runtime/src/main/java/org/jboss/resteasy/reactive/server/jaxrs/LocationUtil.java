package org.jboss.resteasy.reactive.server.jaxrs;

import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.resteasy.reactive.server.core.CurrentRequestManager;
import org.jboss.resteasy.reactive.server.core.Deployment;
import org.jboss.resteasy.reactive.server.core.ResteasyReactiveRequestContext;
import org.jboss.resteasy.reactive.server.spi.ForwardedInfo;
import org.jboss.resteasy.reactive.server.spi.ServerHttpRequest;

final class LocationUtil {

    static URI determineLocation(URI location) {
        if (!location.isAbsolute()) {
            // FIXME: this leaks server stuff onto the client
            ResteasyReactiveRequestContext request = CurrentRequestManager.get();
            if (request != null) {
                ServerHttpRequest req = request.serverRequest();
                try {
                    String host = req.getRequestHost();
                    int port = -1;
                    int index = host.lastIndexOf(":");
                    if (index > -1 && (host.charAt(0) != '[' || index > host.lastIndexOf("]"))) {
                        port = Integer.parseInt(host.substring(index + 1));
                        host = host.substring(0, index);
                    }
                    String prefix = determinePrefix(req, request.getDeployment());
                    // Spec says relative to request, but TCK tests relative to Base URI, so we do that
                    String path = location.toString();
                    if (!path.startsWith("/")) {
                        path = "/" + path;
                    }
                    URI baseUri = new URI(req.getRequestScheme(), null, host, port, null, null, null);
                    location = baseUri.resolve(prefix + path);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return location;
    }

    private static String determinePrefix(ServerHttpRequest serverHttpRequest, Deployment deployment) {
        String prefix = "";
        if (deployment != null) {
            // prefix is already sanitised
            prefix = deployment.getPrefix();
        }
        ForwardedInfo forwardedInfo = serverHttpRequest.getForwardedInfo();
        if (forwardedInfo != null) {
            if ((forwardedInfo.getPrefix() != null) && !forwardedInfo.getPrefix().isEmpty()) {
                String forwardedPrefix = forwardedInfo.getPrefix();
                if (!forwardedPrefix.startsWith("/")) {
                    forwardedPrefix = "/" + forwardedPrefix;
                }
                prefix = forwardedPrefix + prefix;
            }
        }
        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix;
    }

    static URI determineContentLocation(URI location) {
        if (!location.isAbsolute()) {
            ResteasyReactiveRequestContext request = CurrentRequestManager.get();
            if (request != null) {
                // FIXME: this leaks server stuff onto the client
                ServerHttpRequest req = request.serverRequest();
                try {
                    String host = req.getRequestHost();
                    int port = -1;
                    int index = host.lastIndexOf(":");
                    if (index > -1 && (host.charAt(0) != '[' || index > host.lastIndexOf("]"))) {
                        port = Integer.parseInt(host.substring(index + 1));
                        host = host.substring(0, index);
                    }
                    String path = location.toString();
                    if (!path.startsWith("/")) {
                        path = "/" + path;
                    }
                    location = new URI(req.getRequestScheme(), null, host, port, null, null, null)
                            .resolve(path);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return location;
    }
}
