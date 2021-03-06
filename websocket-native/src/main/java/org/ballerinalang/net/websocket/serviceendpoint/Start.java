/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.websocket.serviceendpoint;

import io.ballerina.runtime.api.values.BObject;
import org.ballerinalang.net.http.HttpConnectorPortBindingListener;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpErrorType;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.transport.contract.ServerConnector;
import org.ballerinalang.net.transport.contract.ServerConnectorFuture;
import org.ballerinalang.net.websocket.server.WebSocketServerListener;

import static org.ballerinalang.net.http.HttpConstants.SERVICE_ENDPOINT_CONFIG;

/**
 * Start the Websocket listener instance.
 *
 */
public class Start extends AbstractWebsocketNativeFunction {
    public static Object start(BObject listener) {
        if (!isConnectorStarted(listener)) {
            return startServerConnector(listener);
        }
        return null;
    }

    private static Object startServerConnector(BObject serviceEndpoint) {
        ServerConnector serverConnector = getServerConnector(serviceEndpoint);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
//        BallerinaHTTPConnectorListener httpListener =
//                new BallerinaHTTPConnectorListener(getHttpServicesRegistry(serviceEndpoint),
//                        serviceEndpoint.getMapValue(SERVICE_ENDPOINT_CONFIG));
        WebSocketServerListener wsListener =
                new WebSocketServerListener(getWebSocketServicesRegistry(serviceEndpoint),
                        serviceEndpoint.getMapValue(SERVICE_ENDPOINT_CONFIG));
        HttpConnectorPortBindingListener portBindingListener = new HttpConnectorPortBindingListener();
//        serverConnectorFuture.setHttpConnectorListener(httpListener);
        serverConnectorFuture.setWebSocketConnectorListener(wsListener);
        serverConnectorFuture.setPortBindingEventListener(portBindingListener);

        try {
            serverConnectorFuture.sync();
        } catch (Exception ex) {
            throw HttpUtil.createHttpError("failed to start server connector '"
                    + serverConnector.getConnectorID()
                    + "': " + ex.getMessage(), HttpErrorType.LISTENER_STARTUP_FAILURE);
        }

        serviceEndpoint.addNativeData(HttpConstants.CONNECTOR_STARTED, true);
        return null;
    }
}
