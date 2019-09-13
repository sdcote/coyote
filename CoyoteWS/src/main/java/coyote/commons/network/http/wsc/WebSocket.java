package coyote.commons.network.http.wsc;

import coyote.commons.network.http.wsc.StateManager.CloseInitiator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import static coyote.commons.network.http.wsc.WebSocketState.*;


/**
 * WebSocket.
 *
 * <h3>Create WebSocketFactory</h3>
 *
 * <p>
 * {@link WebSocketFactory} is a factory class that creates
 * {@link WebSocket} instances. The first step is to create a
 * {@code WebSocketFactory} instance.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Create a WebSocketFactory instance.</span>
 * WebSocketFactory factory = new {@link WebSocketFactory#WebSocketFactory()
 * WebSocketFactory()};</pre>
 * </blockquote>
 *
 * <p>
 * By default, {@code WebSocketFactory} uses {@link
 * javax.net.SocketFactory SocketFactory}{@code .}{@link
 * javax.net.SocketFactory#getDefault() getDefault()} for
 * non-secure WebSocket connections ({@code ws:}) and {@link
 * javax.net.ssl.SSLSocketFactory SSLSocketFactory}{@code
 * .}{@link javax.net.ssl.SSLSocketFactory#getDefault()
 * getDefault()} for secure WebSocket connections ({@code
 * wss:}). You can change this default behavior by using
 * {@code WebSocketFactory.}{@link
 * WebSocketFactory#setSocketFactory(javax.net.SocketFactory)
 * setSocketFactory} method, {@code WebSocketFactory.}{@link
 * WebSocketFactory#setSSLSocketFactory(javax.net.ssl.SSLSocketFactory)
 * setSSLSocketFactory} method and {@code WebSocketFactory.}{@link
 * WebSocketFactory#setSSLContext(javax.net.ssl.SSLContext)
 * setSSLContext} method. Note that you don't have to call a {@code
 * setSSL*} method at all if you use the default SSL configuration.
 * Also note that calling {@code setSSLSocketFactory} method has no
 * meaning if you have called {@code setSSLContext} method. See the
 * description of {@code WebSocketFactory.}{@link
 * WebSocketFactory#createSocket(URI) createSocket(URI)} method for
 * details.
 * </p>
 *
 * <p>
 * The following is an example to set a custom SSL context to a
 * {@code WebSocketFactory} instance. (Again, you don't have to call a
 * {@code setSSL*} method if you use the default SSL configuration.)
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Create a custom SSL context.</span>
 * SSLContext context = NaiveSSLContext.getInstance(<span style="color:darkred;">"TLS"</span>);
 *
 * <span style="color: green;">// Set the custom SSL context.</span>
 * factory.{@link WebSocketFactory#setSSLContext(javax.net.ssl.SSLContext)
 * setSSLContext}(context);
 *
 * <span style="color: green;">// Disable manual hostname verification for NaiveSSLContext.
 * //
 * // Manual hostname verification has been enabled since the
 * // version 2.1. Because the verification is executed manually
 * // after Socket.connect(SocketAddress, int) succeeds, the
 * // hostname verification is always executed even if you has
 * // passed an SSLContext which naively accepts any server
 * // certificate. However, this behavior is not desirable in
 * // some cases and you may want to disable the hostname
 * // verification. You can disable the hostname verification
 * // by calling WebSocketFactory.setVerifyHostname(false).</span>
 * factory.{@link WebSocketFactory#setVerifyHostname(boolean) setVerifyHostname}(false);</pre>
 * </blockquote>
 *
 * <p>
 * NaiveSSLContext used in the above example is a factory class to
 * create an {@link javax.net.ssl.SSLContext SSLContext} which naively
 * accepts all certificates without verification. It's enough for testing
 * purposes. When you see an error message
 * "unable to find valid certificate path to requested target" while
 * testing, try {@code NaiveSSLContext}.
 * </p>
 *
 * <h3>HTTP Proxy</h3>
 *
 * <p>
 * If a WebSocket endpoint needs to be accessed via an HTTP proxy,
 * information about the proxy server has to be set to a {@code
 * WebSocketFactory} instance before creating a {@code WebSocket}
 * instance. Proxy settings are represented by {@link ProxySettings}
 * class. A {@code WebSocketFactory} instance has an associated
 * {@code ProxySettings} instance and it can be obtained by calling
 * {@code WebSocketFactory.}{@link WebSocketFactory#getProxySettings()
 * getProxySettings()} method.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Get the associated ProxySettings instance.</span>
 * {@link ProxySettings} settings = factory.{@link
 * WebSocketFactory#getProxySettings() getProxySettings()};</pre>
 * </blockquote>
 *
 * <p>
 * {@code ProxySettings} class has methods to set information about
 * a proxy server such as {@link ProxySettings#setHost(String) setHost}
 * method and {@link ProxySettings#setPort(int) setPort} method. The
 * following is an example to set a secure ({@code https}) proxy
 * server.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Set a proxy server.</span>
 * settings.{@link ProxySettings#setServer(String)
 * setServer}(<span style="color:darkred;">"https://proxy.example.com"</span>);</pre>
 * </blockquote>
 *
 * <p>
 * If credentials are required for authentication at a proxy server,
 * {@link ProxySettings#setId(String) setId} method and {@link
 * ProxySettings#setPassword(String) setPassword} method, or
 * {@link ProxySettings#setCredentials(String, String) setCredentials}
 * method can be used to set the credentials. Note that, however,
 * the current implementation supports only Basic Authentication.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Set credentials for authentication at a proxy server.</span>
 * settings.{@link ProxySettings#setCredentials(String, String)
 * setCredentials}(id, password);
 * </pre>
 * </blockquote>
 *
 * <h3>Create WebSocket</h3>
 *
 * <p>
 * {@link WebSocket} class represents a WebSocket. Its instances are
 * created by calling one of {@code createSocket} methods of a {@link
 * WebSocketFactory} instance. Below is the simplest example to create
 * a {@code WebSocket} instance.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Create a WebSocket. The scheme part can be one of the following:
 * // 'ws', 'wss', 'http' and 'https' (case-insensitive). The user info
 * // part, if any, is interpreted as expected. If a raw socket failed
 * // to be created, an IOException is thrown.</span>
 * WebSocket ws = new {@link WebSocketFactory#WebSocketFactory()
 * WebSocketFactory()}
 *     .{@link WebSocketFactory#createSocket(String)
 * createWebSocket}(<span style="color: darkred;">"ws://localhost/endpoint"</span>);</pre>
 * </blockquote>
 *
 * <p>
 * There are two ways to set a timeout value for socket connection. The
 * first way is to call {@link WebSocketFactory#setConnectionTimeout(int)
 * setConnectionTimeout(int timeout)} method of {@code WebSocketFactory}.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Create a WebSocket factory and set 5000 milliseconds as a timeout
 * // value for socket connection.</span>
 * WebSocketFactory factory = new WebSocketFactory().{@link
 * WebSocketFactory#setConnectionTimeout(int) setConnectionTimeout}(5000);
 *
 * <span style="color: green;">// Create a WebSocket. The timeout value set above is used.</span>
 * WebSocket ws = factory.{@link WebSocketFactory#createSocket(String)
 * createWebSocket}(<span style="color: darkred;">"ws://localhost/endpoint"</span>);</pre>
 * </blockquote>
 *
 * <p>
 * The other way is to give a timeout value to a {@code createSocket} method.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Create a WebSocket factory. The timeout value remains 0.</span>
 * WebSocketFactory factory = new WebSocketFactory();
 *
 * <span style="color: green;">// Create a WebSocket with a socket connection timeout value.</span>
 * WebSocket ws = factory.{@link WebSocketFactory#createSocket(String, int)
 * createWebSocket}(<span style="color: darkred;">"ws://localhost/endpoint"</span>, 5000);</pre>
 * </blockquote>
 *
 * <p>
 * The timeout value is passed to {@link Socket#connect(java.net.SocketAddress, int)
 * connect}{@code (}{@link java.net.SocketAddress SocketAddress}{@code , int)}
 * method of {@link java.net.Socket}.
 * </p>
 *
 * <h3>Register Listener</h3>
 *
 * <p>
 * After creating a {@code WebSocket} instance, you should call {@link
 * #addListener(WebSocketListener)} method to register a {@link
 * WebSocketListener} that receives WebSocket events. {@link
 * WebSocketAdapter} is an empty implementation of {@link
 * WebSocketListener} interface.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Register a listener to receive WebSocket events.</span>
 * ws.{@link #addListener(WebSocketListener) addListener}(new {@link
 * WebSocketAdapter#WebSocketAdapter() WebSocketAdapter()} {
 *     <span style="color: gray;">{@code @}Override</span>
 *     public void {@link WebSocketListener#onTextMessage(WebSocket, String)
 *     onTextMessage}(WebSocket websocket, String message) throws Exception {
 *         <span style="color: green;">// Received a text message.</span>
 *         ......
 *     }
 * });</pre>
 * </blockquote>
 *
 * <p>
 * The table below is the list of callback methods defined in {@code WebSocketListener}
 * interface.
 * </p>
 *
 * <blockquote>
 * <table border="1" cellpadding="5" style="border-collapse: collapse;">
 *   <caption>{@code WebSocketListener} methods</caption>
 *   <thead>
 *     <tr>
 *       <th>Method</th>
 *       <th>Description</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link WebSocketListener#handleCallbackError(WebSocket, Throwable) handleCallbackError}</td>
 *       <td>Called when an {@code on<i>Xxx</i>()} method threw a {@code Throwable}.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onBinaryFrame(WebSocket, WebSocketFrame) onBinaryFrame}</td>
 *       <td>Called when a binary frame was received.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onBinaryMessage(WebSocket, byte[]) onBinaryMessage}</td>
 *       <td>Called when a binary message was received.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onCloseFrame(WebSocket, WebSocketFrame) onCloseFrame}</td>
 *       <td>Called when a close frame was received.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onConnected(WebSocket, Map) onConnected}</td>
 *       <td>Called after the opening handshake succeeded.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onConnectError(WebSocket, WebSocketException) onConnectError}</td>
 *       <td>Called when {@link #connectAsynchronously()} failed.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onContinuationFrame(WebSocket, WebSocketFrame) onContinuationFrame}</td>
 *       <td>Called when a continuation frame was received.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onDisconnected(WebSocket, WebSocketFrame, WebSocketFrame, boolean) onDisconnected}</td>
 *       <td>Called after a WebSocket connection was closed.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onError(WebSocket, WebSocketException) onError}</td>
 *       <td>Called when an error occurred.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onFrame(WebSocket, WebSocketFrame) onFrame}</td>
 *       <td>Called when a frame was received.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onFrameError(WebSocket, WebSocketException, WebSocketFrame) onFrameError}</td>
 *       <td>Called when a frame failed to be read.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onFrameSent(WebSocket, WebSocketFrame) onFrameSent}</td>
 *       <td>Called when a frame was sent.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onFrameUnsent(WebSocket, WebSocketFrame) onFrameUnsent}</td>
 *       <td>Called when a frame was not sent.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onMessageDecompressionError(WebSocket, WebSocketException, byte[]) onMessageDecompressionError}</td>
 *       <td>Called when a message failed to be decompressed.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onMessageError(WebSocket, WebSocketException, List) onMessageError}</td>
 *       <td>Called when a message failed to be constructed.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onPingFrame(WebSocket, WebSocketFrame) onPingFrame}</td>
 *       <td>Called when a ping frame was received.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onPongFrame(WebSocket, WebSocketFrame) onPongFrame}</td>
 *       <td>Called when a pong frame was received.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onSendError(WebSocket, WebSocketException, WebSocketFrame) onSendError}</td>
 *       <td>Called when an error occurred on sending a frame.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onSendingFrame(WebSocket, WebSocketFrame) onSendingFrame}</td>
 *       <td>Called before a frame is sent.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onSendingHandshake(WebSocket, String, List) onSendingHandshake}</td>
 *       <td>Called before an opening handshake is sent.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onStateChanged(WebSocket, WebSocketState) onStateChanged}</td>
 *       <td>Called when the state of WebSocket changed.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onTextFrame(WebSocket, WebSocketFrame) onTextFrame}</td>
 *       <td>Called when a text frame was received.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onTextMessage(WebSocket, String) onTextMessage}</td>
 *       <td>Called when a text message was received.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onTextMessageError(WebSocket, WebSocketException, byte[]) onTextMessageError}</td>
 *       <td>Called when a text message failed to be constructed.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onThreadCreated(WebSocket, ThreadType, Thread) onThreadCreated}</td>
 *       <td>Called after a thread was created.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onThreadStarted(WebSocket, ThreadType, Thread) onThreadStarted}</td>
 *       <td>Called at the beginning of a thread's {@code run()} method.
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onThreadStopping(WebSocket, ThreadType, Thread) onThreadStopping}</td>
 *       <td>Called at the end of a thread's {@code run()} method.
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onUnexpectedError(WebSocket, WebSocketException) onUnexpectedError}</td>
 *       <td>Called when an uncaught throwable was detected.</td>
 *     </tr>
 *   </tbody>
 * </table>
 * </blockquote>
 *
 * <h3>Configure WebSocket</h3>
 *
 * <p>
 * Before starting a WebSocket <a href="https://tools.ietf.org/html/rfc6455#section-4"
 * >opening handshake</a> with the server, you can configure the
 * {@code WebSocket} instance by using the following methods.
 * </p>
 *
 * <blockquote>
 * <table border="1" cellpadding="5" style="border-collapse: collapse;">
 *   <caption>Methods for Configuration</caption>
 *   <thead>
 *     <tr>
 *       <th>METHOD</th>
 *       <th>DESCRIPTION</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link #addProtocol(String) addProtocol}</td>
 *       <td>Adds an element to {@code Sec-WebSocket-Protocol}</td>
 *     </tr>
 *     <tr>
 *       <td>{@link #addExtension(WebSocketExtension) addExtension}</td>
 *       <td>Adds an element to {@code Sec-WebSocket-Extensions}</td>
 *     </tr>
 *     <tr>
 *       <td>{@link #addHeader(String, String) addHeader}</td>
 *       <td>Adds an arbitrary HTTP header.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link #setUserInfo(String, String) setUserInfo}</td>
 *       <td>Adds {@code Authorization} header for Basic Authentication.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link #getSocket() getSocket}</td>
 *       <td>Gets the underlying {@link Socket} instance to configure it.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link #setExtended(boolean) setExtended}</td>
 *       <td>Disables validity checks on RSV1/RSV2/RSV3 and opcode.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link #setFrameQueueSize(int) setFrameQueueSize}</td>
 *       <td>Set the size of the frame queue for <a href="#congestion_control">congestion control</a>.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link #setMaxPayloadSize(int) setMaxPayloadSize}</td>
 *       <td>Set the <a href="#maximum_payload_size">maximum payload size</a>.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link #setMissingCloseFrameAllowed(boolean) setMissingCloseFrameAllowed}</td>
 *       <td>Set whether to allow the server to close the connection without sending a close frame.</td>
 *     </tr>
 *   </tbody>
 * </table>
 * </blockquote>
 *
 * <h3>Connect To Server</h3>
 *
 * <p>
 * By calling {@link #connect()} method, connection to the server is
 * established and a WebSocket opening handshake is performed
 * synchronously. If an error occurred during the handshake,
 * a {@link WebSocketException} would be thrown. Instead, when the
 * handshake succeeds, the {@code connect()} implementation creates
 * threads and starts them to read and write WebSocket frames
 * asynchronously.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> try
 * {
 *     <span style="color: green;">// Connect to the server and perform an opening handshake.</span>
 *     <span style="color: green;">// This method blocks until the opening handshake is finished.</span>
 *     ws.{@link #connect()};
 * }
 * catch ({@link OpeningHandshakeException} e)
 * {
 *     <span style="color: green;">// A violation against the WebSocket protocol was detected</span>
 *     <span style="color: green;">// during the opening handshake.</span>
 * }
 * catch ({@link HostnameUnverifiedException} e)
 * {
 *     <span style="color: green;">// The certificate of the peer does not match the expected hostname.</span>
 * }
 * catch ({@link WebSocketException} e)
 * {
 *     <span style="color: green;">// Failed to establish a WebSocket connection.</span>
 * }</pre>
 * </blockquote>
 *
 * <p>
 * In some cases, {@code connect()} method throws {@link OpeningHandshakeException}
 * which is a subclass of {@code WebSocketException} (since version 1.19).
 * {@code OpeningHandshakeException} provides additional methods such as
 * {@link OpeningHandshakeException#getStatusLine() getStatusLine()},
 * {@link OpeningHandshakeException#getHeaders() getHeaders()} and
 * {@link OpeningHandshakeException#getBody() getBody()} to access the
 * response from a server. The following snippet is an example to print
 * information that the exception holds.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> catch ({@link OpeningHandshakeException} e)
 * {
 *     <span style="color: green;">// Status line.</span>
 *     {@link StatusLine} sl = e.{@link OpeningHandshakeException#getStatusLine() getStatusLine()};
 *     System.out.println(<span style="color:darkred;">"=== Status Line ==="</span>);
 *     System.out.format(<span style="color:darkred;">"HTTP Version  = %s\n"</span>, sl.{@link StatusLine#getHttpVersion() getHttpVersion()});
 *     System.out.format(<span style="color:darkred;">"Status Code   = %d\n"</span>, sl.{@link StatusLine#getStatusCode() getStatusCode()});
 *     System.out.format(<span style="color:darkred;">"Reason Phrase = %s\n"</span>, sl.{@link StatusLine#getReasonPhrase() getReasonPhrase()});
 *
 *     <span style="color: green;">// HTTP headers.</span>
 *     Map&lt;String, List&lt;String&gt;&gt; headers = e.{@link OpeningHandshakeException#getHeaders() getHeaders()};
 *     System.out.println(<span style="color:darkred;">"=== HTTP Headers ==="</span>);
 *     for (Map.Entry&lt;String, List&lt;String&gt;&gt; entry : headers.entrySet())
 *     {
 *         <span style="color: green;">// Header name.</span>
 *         String name = entry.getKey();
 *
 *         <span style="color: green;">// Values of the header.</span>
 *         List&lt;String&gt; values = entry.getValue();
 *
 *         if (values == null || values.size() == 0)
 *         {
 *             <span style="color: green;">// Print the name only.</span>
 *             System.out.println(name);
 *             continue;
 *         }
 *
 *         for (String value : values)
 *         {
 *             <span style="color: green;">// Print the name and the value.</span>
 *             System.out.format(<span style="color:darkred;">"%s: %s\n"</span>, name, value);
 *         }
 *     }
 * }</pre>
 * </blockquote>
 *
 * <p>
 * Also, {@code connect()} method throws {@link HostnameUnverifiedException}
 * which is a subclass of {@code WebSocketException} (since version 2.1) when
 * the certificate of the peer does not match the expected hostname.
 * </p>
 *
 * <h3>Connect To Server Asynchronously</h3>
 *
 * <p>
 * The simplest way to call {@code connect()} method asynchronously is to
 * use {@link #connectAsynchronously()} method. The implementation of the
 * method creates a thread and calls {@code connect()} method in the thread.
 * When the {@code connect()} call failed, {@link
 * WebSocketListener#onConnectError(WebSocket, WebSocketException)
 * onConnectError()} of {@code WebSocketListener} would be called. Note that
 * {@code onConnectError()} is called only when {@code connectAsynchronously()}
 * was used and the {@code connect()} call executed in the background thread
 * failed. Neither direct synchronous {@code connect()} nor
 * {@link WebSocket#connect(java.util.concurrent.ExecutorService)
 * connect(ExecutorService)} (described below) will trigger the callback method.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Connect to the server asynchronously.</span>
 * ws.{@link #connectAsynchronously()};
 * </pre>
 * </blockquote>
 *
 * <p>
 * Another way to call {@code connect()} method asynchronously is to use
 * {@link #connect(ExecutorService)} method. The method performs a WebSocket
 * opening handshake asynchronously using the given {@link ExecutorService}.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Prepare an ExecutorService.</span>
 * {@link ExecutorService} es = {@link java.util.concurrent.Executors Executors}.{@link
 * java.util.concurrent.Executors#newSingleThreadExecutor() newSingleThreadExecutor()};
 *
 * <span style="color: green;">// Connect to the server asynchronously.</span>
 * {@link Future}{@code <WebSocket>} future = ws.{@link #connect(ExecutorService) connect}(es);
 *
 * try
 * {
 *     <span style="color: green;">// Wait for the opening handshake to complete.</span>
 *     future.get();
 * }
 * catch ({@link java.util.concurrent.ExecutionException ExecutionException} e)
 * {
 *     if (e.getCause() instanceof {@link WebSocketException})
 *     {
 *         ......
 *     }
 * }</pre>
 * </blockquote>
 *
 * <p>
 * The implementation of {@code connect(ExecutorService)} method creates
 * a {@link java.util.concurrent.Callable Callable}{@code <WebSocket>}
 * instance by calling {@link #connectable()} method and passes the
 * instance to {@link ExecutorService#submit(Callable) submit(Callable)}
 * method of the given {@code ExecutorService}. What the implementation
 * of {@link Callable#call() call()} method of the {@code Callable}
 * instance does is just to call the synchronous {@code connect()}.
 * </p>
 *
 * <h3>Send Frames</h3>
 *
 * <p>
 * WebSocket frames can be sent by {@link #sendFrame(WebSocketFrame)}
 * method. Other {@code send<i>Xxx</i>} methods such as {@link
 * #sendText(String)} are aliases of {@code sendFrame} method. All of
 * the {@code send<i>Xxx</i>} methods work asynchronously.
 * However, under some conditions, {@code send<i>Xxx</i>} methods
 * may block. See <a href="#congestion_control">Congestion Control</a>
 * for details.
 * </p>
 *
 * <p>
 * Below
 * are some examples of {@code send<i>Xxx</i>} methods. Note that
 * in normal cases, you don't have to call {@link #sendClose()} method
 * and {@link #sendPong()} (or their variants) explicitly because they
 * are called automatically when appropriate.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Send a text frame.</span>
 * ws.{@link #sendText(String) sendText}(<span style="color: darkred;">"Hello."</span>);
 *
 * <span style="color: green;">// Send a binary frame.</span>
 * byte[] binary = ......;
 * ws.{@link #sendBinary(byte[]) sendBinary}(binary);
 *
 * <span style="color: green;">// Send a ping frame.</span>
 * ws.{@link #sendPing(String) sendPing}(<span style="color: darkred;">"Are you there?"</span>);</pre>
 * </blockquote>
 *
 * <p>
 * If you want to send fragmented frames, you have to know the details
 * of the specification (<a href="https://tools.ietf.org/html/rfc6455#section-5.4"
 * >5.4. Fragmentation</a>). Below is an example to send a text message
 * ({@code "How are you?"}) which consists of 3 fragmented frames.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// The first frame must be either a text frame or a binary frame.
 * // And its FIN bit must be cleared.</span>
 * WebSocketFrame firstFrame = WebSocketFrame
 *     .{@link WebSocketFrame#createTextFrame(String)
 *     createTextFrame}(<span style="color: darkred;">"How "</span>)
 *     .{@link WebSocketFrame#setFin(boolean) setFin}(false);
 *
 * <span style="color: green;">// Subsequent frames must be continuation frames. The FIN bit of
 * // all continuation frames except the last one must be cleared.
 * // Note that the FIN bit of frames returned from
 * // WebSocketFrame.createContinuationFrame methods is cleared, so
 * // the example below does not clear the FIN bit explicitly.</span>
 * WebSocketFrame secondFrame = WebSocketFrame
 *     .{@link WebSocketFrame#createContinuationFrame(String)
 *     createContinuationFrame}(<span style="color: darkred;">"are "</span>);
 *
 * <span style="color: green;">// The last frame must be a continuation frame with the FIN bit set.
 * // Note that the FIN bit of frames returned from
 * // WebSocketFrame.createContinuationFrame methods is cleared, so
 * // the FIN bit of the last frame must be set explicitly.</span>
 * WebSocketFrame lastFrame = WebSocketFrame
 *     .{@link WebSocketFrame#createContinuationFrame(String)
 *     createContinuationFrame}(<span style="color: darkred;">"you?"</span>)
 *     .{@link WebSocketFrame#setFin(boolean) setFin}(true);
 *
 * <span style="color: green;">// Send a text message which consists of 3 frames.</span>
 * ws.{@link #sendFrame(WebSocketFrame) sendFrame}(firstFrame)
 *   .{@link #sendFrame(WebSocketFrame) sendFrame}(secondFrame)
 *   .{@link #sendFrame(WebSocketFrame) sendFrame}(lastFrame);</pre>
 * </blockquote>
 *
 * <p>
 * Alternatively, the same as above can be done like this.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Send a text message which consists of 3 frames.</span>
 * ws.{@link #sendText(String, boolean) sendText}(<span style="color: darkred;">"How "</span>, false)
 *   .{@link #sendContinuation(String) sendContinuation}(<span style="color: darkred;">"are "</span>)
 *   .{@link #sendContinuation(String, boolean) sendContinuation}(<span style="color: darkred;">"you?"</span>, true);</pre>
 * </blockquote>
 *
 * <h3>Send Ping/Pong Frames Periodically</h3>
 *
 * <p>
 * You can send ping frames periodically by calling {@link #setPingInterval(long)
 * setPingInterval} method with an interval in milliseconds between ping frames.
 * This method can be called both before and after {@link #connect()} method.
 * Passing zero stops the periodical sending.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Send a ping per 60 seconds.</span>
 * ws.{@link #setPingInterval(long) setPingInterval}(60 * 1000);
 *
 * <span style="color: green;">// Stop the periodical sending.</span>
 * ws.{@link #setPingInterval(long) setPingInterval}(0);</pre>
 * </blockquote>
 *
 * <p>
 * Likewise, you can send pong frames periodically by calling {@link
 * #setPongInterval(long) setPongInterval} method. "<i>A Pong frame MAY be sent
 * <b>unsolicited</b>."</i> (<a href="https://tools.ietf.org/html/rfc6455#section-5.5.3"
 * >RFC 6455, 5.5.3. Pong</a>)
 * </p>
 *
 * <p>
 * You can customize payload of ping/pong frames that are sent automatically by using
 * {@link #setPingPayloadGenerator(PayloadGenerator)} and
 * {@link #setPongPayloadGenerator(PayloadGenerator)} methods. Both methods take an
 * instance of {@link PayloadGenerator} interface. The following is an example to
 * use the string representation of the current date as payload of ping frames.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> ws.{@link #setPingPayloadGenerator(PayloadGenerator)
 * setPingPayloadGenerator}(new {@link PayloadGenerator} () {
 *     <span style="color: gray;">{@code @}Override</span>
 *     public byte[] generate() {
 *         <span style="color: green;">// The string representation of the current date.</span>
 *         return new Date().toString().getBytes();
 *     }
 * });</pre>
 * </blockquote>
 *
 * <p>
 * Note that the maximum payload length of control frames (e.g. ping frames) is 125.
 * Therefore, the length of a byte array returned from {@link PayloadGenerator#generate()
 * generate()} method must not exceed 125.
 * </p>
 *
 * <h3>Auto Flush</h3>
 *
 * <p>
 * By default, a frame is automatically flushed to the server immediately after
 * {@link #sendFrame(WebSocketFrame) sendFrame} method is executed. This automatic
 * flush can be disabled by calling {@link #setAutoFlush(boolean) setAutoFlush}{@code
 * (false)}.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Disable auto-flush.</span>
 * ws.{@link #setAutoFlush(boolean) setAutoFlush}(false);</pre>
 * </blockquote>
 *
 * <p>
 * To flush frames manually, call {@link #flush()} method. Note that this method
 * works asynchronously.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Flush frames to the server manually.</span>
 * ws.{@link #flush()};</pre>
 * </blockquote>
 *
 * <h3 id="congestion_control">Congestion Control</h3>
 *
 * <p>
 * {@code send<i>Xxx</i>} methods queue a {@link WebSocketFrame} instance to the
 * internal queue. By default, no upper limit is imposed on the queue size, so
 * {@code send<i>Xxx</i>} methods do not block. However, this behavior may cause
 * a problem if your WebSocket client application sends too many WebSocket frames in
 * a short time for the WebSocket server to process. In such a case, you may want
 * {@code send<i>Xxx</i>} methods to block when many frames are queued.
 * </p>
 *
 * <p>
 * You can set an upper limit on the internal queue by calling {@link #setFrameQueueSize(int)}
 * method. As a result, if the number of frames in the queue has reached the upper limit
 * when a {@code send<i>Xxx</i>} method is called, the method blocks until the
 * queue gets spaces. The code snippet below is an example to set 5 as the upper limit
 * of the internal frame queue.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Set 5 as the frame queue size.</span>
 * ws.{@link #setFrameQueueSize(int) setFrameQueueSize}(5);</pre>
 * </blockquote>
 *
 * <p>
 * Note that under some conditions, even if the queue is full, {@code send<i>Xxx</i>}
 * methods do not block. For example, in the case where the thread to send frames
 * ({@code WritingThread}) is going to stop or has already stopped. In addition,
 * method calls to send a <a href="https://tools.ietf.org/html/rfc6455#section-5.5"
 * >control frame</a> (e.g. {@link #sendClose()} and {@link #sendPing()}) do not block.
 * </p>
 *
 * <h3 id="maximum_payload_size">Maximum Payload Size</h3>
 *
 * <p>
 * You can set an upper limit on the payload size of WebSocket frames by calling
 * {@link #setMaxPayloadSize(int)} method with a positive value. Text, binary and
 * continuation frames whose payload size is bigger than the maximum payload size
 * you have set will be split into multiple frames.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Set 1024 as the maximum payload size.</span>
 * ws.{@link #setMaxPayloadSize(int) setMaxPayloadSize}(1024);</pre>
 * </blockquote>
 *
 * <p>
 * Control frames (close, ping and pong frames) are never split as per the specification.
 * </p>
 *
 * <p>
 * If permessage-deflate extension is enabled and if the payload size of a WebSocket
 * frame after compression does not exceed the maximum payload size, the WebSocket
 * frame is not split even if the payload size before compression execeeds the
 * maximum payload size.
 * </p>
 *
 * <h3 id="compression">Compression</h3>
 *
 * <p>
 * The <strong>permessage-deflate</strong> extension (<a href=
 * "http://tools.ietf.org/html/rfc7692">RFC 7692</a>) has been supported
 * since the version 1.17. To enable the extension, call {@link #addExtension(String)
 * addExtension} method with {@code "permessage-deflate"}.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"><span style="color: green;"> // Enable "permessage-deflate" extension (RFC 7692).</span>
 * ws.{@link #addExtension(String) addExtension}({@link WebSocketExtension#PERMESSAGE_DEFLATE});</pre>
 * </blockquote>
 *
 * <h3>Missing Close Frame</h3>
 *
 * <p>
 * Some server implementations close a WebSocket connection without sending a
 * <a href="https://tools.ietf.org/html/rfc6455#section-5.5.1">close frame</a> to
 * a client in some cases. Strictly speaking, this is a violation against the
 * specification (<a href=
 * "https://tools.ietf.org/html/rfc6455#section-5.5.1">RFC 6455</a>). However, this
 * library has allowed the behavior by default since the version 1.29. Even if the
 * end of the input stream of a WebSocket connection were reached without a close
 * frame being received, it would trigger neither {@link
 * WebSocketListener#onError(WebSocket, WebSocketException) onError()} method nor
 * {@link WebSocketListener#onFrameError(WebSocket, WebSocketException, WebSocketFrame)
 * onFrameError()} method of {@link WebSocketListener}. If you want to make a
 * {@code WebSocket} instance report an error in the case, pass {@code false} to
 * {@link #setMissingCloseFrameAllowed(boolean)} method.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"><span style="color: green;"
 * > // Make this library report an error when the end of the input stream
 * // of the WebSocket connection is reached before a close frame is read.</span>
 * ws.{@link #setMissingCloseFrameAllowed(boolean) setMissingCloseFrameAllowed}(false);</pre>
 * </blockquote>
 *
 * <h3>Disconnect WebSocket</h3>
 *
 * <p>
 * Before a WebSocket is closed, a closing handshake is performed. A closing handshake
 * is started (1) when the server sends a close frame to the client or (2) when the
 * client sends a close frame to the server. You can start a closing handshake by calling
 * {@link #disconnect()} method (or by sending a close frame manually).
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Close the WebSocket connection.</span>
 * ws.{@link #disconnect()};</pre>
 * </blockquote>
 *
 * <p>
 * {@code disconnect()} method has some variants. If you want to change the close code
 * and the reason phrase of the close frame that this client will send to the server,
 * use a variant method such as {@link #disconnect(int, String)}. {@code disconnect()}
 * method itself is an alias of {@code disconnect(}{@link WebSocketCloseCode}{@code
 * .NORMAL, null)}.
 * </p>
 *
 * <h3>Reconnection</h3>
 *
 * <p>
 * {@code connect()} method can be called at most only once regardless of whether the
 * method succeeded or failed. If you want to re-connect to the WebSocket endpoint,
 * you have to create a new {@code WebSocket} instance again by calling one of {@code
 * createSocket} methods of a {@code WebSocketFactory}. You may find {@link #recreate()}
 * method useful if you want to create a new {@code WebSocket} instance that has the
 * same settings as the original instance. Note that, however, settings you made on
 * the raw socket of the original {@code WebSocket} instance are not copied.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Create a new WebSocket instance and connect to the same endpoint.</span>
 * ws = ws.{@link #recreate()}.{@link #connect()};</pre>
 * </blockquote>
 *
 * <p>
 * There is a variant of {@code recreate()} method that takes a timeout value for
 * socket connection. If you want to use a timeout value that is different from the
 * one used when the existing {@code WebSocket} instance was created, use {@link
 * #recreate(int) recreate(int timeout)} method.
 * </p>
 *
 * <p>
 * Note that you should not trigger reconnection in {@link
 * WebSocketListener#onError(WebSocket, WebSocketException) onError()} method
 * because {@code onError()} may be called multiple times due to one error. Instead,
 * {@link WebSocketListener#onDisconnected(WebSocket, WebSocketFrame, WebSocketFrame,
 * boolean) onDisconnected()} is the right place to trigger reconnection.
 * </p>
 *
 * <p>
 * Also note that the reason I use an expression of <i>"to trigger reconnection"</i>
 * instead of <i>"to call {@code recreate().connect()}"</i> is that I myself
 * won't do it <i>synchronously</i> in {@code WebSocketListener} callback
 * methods but will just schedule reconnection or will just go to the top of a kind
 * of <i>application loop</i> that repeats to establish a WebSocket connection until
 * it succeeds.
 * </p>
 *
 * <h3>Error Handling</h3>
 *
 * <p>
 * {@code WebSocketListener} has some {@code onXxxError()} methods such as {@link
 * WebSocketListener#onFrameError(WebSocket, WebSocketException, WebSocketFrame)
 * onFrameError()} and {@link
 * WebSocketListener#onSendError(WebSocket, WebSocketException, WebSocketFrame)
 * onSendError()}. Among such methods, {@link
 * WebSocketListener#onError(WebSocket, WebSocketException) onError()} is a special
 * one. It is always called before any other {@code onXxxError()} is called. For
 * example, in the implementation of {@code run()} method of {@code ReadingThread},
 * {@code Throwable} is caught and {@code onError()} and {@link
 * WebSocketListener#onUnexpectedError(WebSocket, WebSocketException)
 * onUnexpectedError()} are called in this order. The following is the implementation.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: gray;">{@code @}Override</span>
 * public void run()
 * {
 *     try
 *     {
 *         main();
 *     }
 *     catch (Throwable t)
 *     {
 *         <span style="color: green;">// An uncaught throwable was detected in the reading thread.</span>
 *         {@link WebSocketException} cause = new WebSocketException(
 *             {@link WebSocketError}.{@link WebSocketError#UNEXPECTED_ERROR_IN_READING_THREAD UNEXPECTED_ERROR_IN_READING_THREAD},
 *             <span style="color: darkred;">"An uncaught throwable was detected in the reading thread"</span>, t);
 *
 *         <span style="color: green;">// Notify the listeners.</span>
 *         ListenerManager manager = mWebSocket.getListenerManager();
 *         manager.callOnError(cause);
 *         manager.callOnUnexpectedError(cause);
 *     }
 * }</pre>
 * </blockquote>
 *
 * <p>
 * So, you can handle all error cases in {@code onError()} method. However, note
 * that {@code onError()} may be called multiple times for one error cause, so don't
 * try to trigger reconnection in {@code onError()}. Instead, {@link
 * WebSocketListener#onDisconnected(WebSocket, WebSocketFrame, WebSocketFrame, boolean)
 * onDiconnected()} is the right place to trigger reconnection.
 * </p>
 *
 * <p>
 * All {@code onXxxError()} methods receive a {@link WebSocketException} instance
 * as the second argument (the first argument is a {@code WebSocket} instance). The
 * exception class provides {@link WebSocketException#getError() getError()} method
 * which returns a {@link WebSocketError} enum entry. Entries in {@code WebSocketError}
 * enum are possible causes of errors that may occur in the implementation of this
 * library. The error causes are so granular that they can make it easy for you to
 * find the root cause when an error occurs.
 * </p>
 *
 * <p>
 * {@code Throwable}s thrown by implementations of {@code onXXX()} callback methods
 * are passed to {@link WebSocketListener#handleCallbackError(WebSocket, Throwable)
 * handleCallbackError()} of {@code WebSocketListener}.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: gray;">{@code @}Override</span>
 * public void {@link WebSocketListener#handleCallbackError(WebSocket, Throwable)
 * handleCallbackError}(WebSocket websocket, Throwable cause) throws Exception {
 *     <span style="color: green;">// Throwables thrown by onXxx() callback methods come here.</span>
 * }</pre>
 * </blockquote>
 *
 * <h3>Thread Callbacks</h3>
 *
 * <p>
 * Some threads are created internally in the implementation of {@code WebSocket}.
 * Known threads are as follows.
 * </p>
 *
 * <blockquote>
 * <table border="1" cellpadding="5" style="border-collapse: collapse;">
 *   <caption>Internal Threads</caption>
 *   <thead>
 *     <tr>
 *       <th>THREAD TYPE</th>
 *       <th>DESCRIPTION</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link ThreadType#READING_THREAD READING_THREAD}</td>
 *       <td>A thread which reads WebSocket frames from the server.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ThreadType#WRITING_THREAD WRITING_THREAD}</td>
 *       <td>A thread which sends WebSocket frames to the server.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ThreadType#CONNECT_THREAD CONNECT_THREAD}</td>
 *       <td>A thread which calls {@link WebSocket#connect()} asynchronously.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ThreadType#FINISH_THREAD FINISH_THREAD}</td>
 *       <td>A thread which does finalization of a {@code WebSocket} instance.</td>
 *     </tr>
 *   </tbody>
 * </table>
 * </blockquote>
 *
 * <p>
 * The following callback methods of {@link WebSocketListener} are called according
 * to the life cycle of the threads.
 * </p>
 *
 * <blockquote>
 * <table border="1" cellpadding="5" style="border-collapse: collapse;">
 *   <caption>Thread Callbacks</caption>
 *   <thead>
 *     <tr>
 *       <th>METHOD</th>
 *       <th>DESCRIPTION</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link WebSocketListener#onThreadCreated(WebSocket, ThreadType, Thread) onThreadCreated()}</td>
 *       <td>Called after a thread was created.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onThreadStarted(WebSocket, ThreadType, Thread) onThreadStarted()}</td>
 *       <td>Called at the beginning of the thread's {@code run()} method.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onThreadStopping(WebSocket, ThreadType, Thread) onThreadStopping()}</td>
 *       <td>Called at the end of the thread's {@code run()} method.</td>
 *     </tr>
 *   </tbody>
 * </table>
 * </blockquote>
 *
 * <p>
 * For example, if you want to change the name of the reading thread,
 * implement {@link WebSocketListener#onThreadCreated(WebSocket, ThreadType, Thread)
 * onThreadCreated()} method like below.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: gray;">{@code @}Override</span>
 * public void {@link WebSocketListener#onThreadCreated(WebSocket, ThreadType, Thread)
 * onThreadCreated}(WebSocket websocket, {@link ThreadType} type, Thread thread)
 * {
 *     if (type == ThreadType.READING_THREAD)
 *     {
 *         thread.setName(<span style="color: darkred;">"READING_THREAD"</span>);
 *     }
 * }</pre>
 * </blockquote>
 *
 * @see <a href="https://tools.ietf.org/html/rfc6455">RFC 6455 (The WebSocket Protocol)</a>
 * @see <a href="https://tools.ietf.org/html/rfc7692">RFC 7692 (Compression Extensions for WebSocket)</a>
 */
public class WebSocket {
  private static final long DEFAULT_CLOSE_DELAY = 10 * 1000L;
  private final WebSocketFactory webSocketFactory;
  private final SocketConnector socketConnector;
  private final StateManager stateManager;
  private final ListenerManager listenerManager;
  private final PingSender pingSender;
  private final PongSender pongSender;
  private final Object threadMutex = new Object();
  private final Object onConnectedCalledMutex = new Object();
  private HandshakeBuilder handshakeBuilder;
  private WebSocketInputStream socketInput;
  private WebSocketOutputStream socketOutput;
  private ReadingThread readThread;
  private WritingThread writeThread;
  private Map<String, List<String>> serverHeaders;
  private List<WebSocketExtension> agreedExtensions;
  private String agreedProtocol;
  private boolean mExtended;
  private boolean autoFlush = true;
  private boolean missingCloseFrameAllowed = true;
  private int frameQueueSize;
  private int maxPayloadSize;
  private boolean onConnectedCalled;
  private boolean readingThreadStarted;
  private boolean writingThreadStarted;
  private boolean readingThreadFinished;
  private boolean writingThreadFinished;
  private WebSocketFrame serverCloseFrame;
  private WebSocketFrame clientCloseFrame;
  private PerMessageCompressionExtension perMessageCompressionExtension;


  WebSocket(final WebSocketFactory factory, final boolean secure, final String userInfo, final String host, final String path, final SocketConnector connector) {
    webSocketFactory = factory;
    socketConnector = connector;
    stateManager = new StateManager();
    handshakeBuilder = new HandshakeBuilder(secure, userInfo, host, path);
    listenerManager = new ListenerManager(this);
    pingSender = new PingSender(this, new CounterPayloadGenerator());
    pongSender = new PongSender(this, new CounterPayloadGenerator());
  }


  /**
   * Generate a value for Sec-WebSocket-Key.
   *
   * <blockquote>
   * <p><i>The request MUST include a header field with the name
   * Sec-WebSocket-Key. The value of this header field MUST be a nonce
   * consisting of a randomly selected 16-byte value that has been base64-
   * encoded (see Section 4 of RFC 4648). The nonce MUST be selected randomly
   * for each connection.</i>
   * </blockquote>
   *
   * @return A randomly generated WebSocket key.
   */
  private static String generateWebSocketKey() {
    // "16-byte value"
    final byte[] data = new byte[16];

    // "randomly selected"
    WebSocketUtil.getRandomBytes(data);

    // "base64-encoded"
    return Base64.encode(data);
  }


  /**
   * Add a value for {@code Sec-WebSocket-Extension}.
   *
   * <p>The input string should comply with the format described in
   * <a href= "https://tools.ietf.org/html/rfc6455#section-9.1">9.1.
   * Negotiating Extensions</a> in
   * <a href="https://tools.ietf.org/html/rfc6455">RFC 6455</a>.
   *
   * @param extension A string that represents a WebSocket extension. If it
   *                  does not comply with RFC 6455, no value is added to {@code
   *                  Sec-WebSocket-Extension}.
   * @return {@code this} object.
   */
  public WebSocket addExtension(final String extension) {
    handshakeBuilder.addExtension(extension);
    return this;
  }


  /**
   * Add a value for {@code Sec-WebSocket-Extension}.
   *
   * @param extension An extension. {@code null} is silently ignored.
   * @return {@code this} object.
   */
  public WebSocket addExtension(final WebSocketExtension extension) {
    handshakeBuilder.addExtension(extension);
    return this;
  }


  /**
   * Add a pair of extra HTTP header.
   *
   * @param name  An HTTP header name. When {@code null} or an empty string is
   *              given, no header is added.
   * @param value The value of the HTTP header.
   * @return {@code this} object.
   */
  public WebSocket addHeader(final String name, final String value) {
    handshakeBuilder.addHeader(name, value);
    return this;
  }


  /**
   * Add a listener to receive events on this WebSocket.
   *
   * @param listener A listener to add.
   * @return {@code this} object.
   */
  public WebSocket addListener(final WebSocketListener listener) {
    listenerManager.addListener(listener);
    return this;
  }


  /**
   * Add listeners.
   *
   * @param listeners Listeners to add. {@code null} is silently ignored.
   *                  {@code null} elements in the list are also ignored.
   * @return {@code this} object.
   */
  public WebSocket addListeners(final List<WebSocketListener> listeners) {
    listenerManager.addListeners(listeners);
    return this;
  }


  /**
   * Add a value for {@code Sec-WebSocket-Protocol}.
   *
   * @param protocol A protocol name.
   * @return {@code this} object.
   * @throws IllegalArgumentException The protocol name is invalid. A protocol
   *                                  name must be a non-empty string with characters in the range
   *                                  U+0021 to U+007E not including separator characters.
   */
  public WebSocket addProtocol(final String protocol) {
    handshakeBuilder.addProtocol(protocol);
    return this;
  }


  /**
   * Call {@link WebSocketListener#onConnected(WebSocket, Map)} method
   * of the registered listeners if it has not been called yet. Either
   * the reading thread or the writing thread calls this method.
   */
  private void callOnConnectedIfNotYet() {
    synchronized (onConnectedCalledMutex) {
      // If onConnected() has already been called.
      if (onConnectedCalled) {
        // Do not call onConnected() twice.
        return;
      }

      onConnectedCalled = true;
    }

    // Notify the listeners that the handshake succeeded.
    listenerManager.callOnConnected(serverHeaders);
  }


  private void changeStateOnConnect() throws WebSocketException {
    synchronized (stateManager) {
      // If the current state is not CREATED.
      if (stateManager.getState() != CREATED) {
        throw new WebSocketException(WebSocketError.NOT_IN_CREATED_STATE, "The current state of the WebSocket is not CREATED.");
      }

      // Change the state to CONNECTING.
      stateManager.setState(CONNECTING);
    }

    // Notify the listeners of the state change.
    listenerManager.callOnStateChanged(CONNECTING);
  }


  /**
   * Remove all extensions from {@code Sec-WebSocket-Extension}.
   *
   * @return {@code this} object.
   */
  public WebSocket clearExtensions() {
    handshakeBuilder.clearExtensions();
    return this;
  }


  /**
   * Clear all extra HTTP headers.
   *
   * @return {@code this} object.
   */
  public WebSocket clearHeaders() {
    handshakeBuilder.clearHeaders();
    return this;
  }


  /**
   * Remove all the listeners from this WebSocket.
   *
   * @return {@code this} object.
   */
  public WebSocket clearListeners() {
    listenerManager.clearListeners();
    return this;
  }


  /**
   * Remove all protocols from {@code Sec-WebSocket-Protocol}.
   *
   * @return {@code this} object.
   */
  public WebSocket clearProtocols() {
    handshakeBuilder.clearProtocols();
    return this;
  }


  /**
   * Clear the credentials to connect to the WebSocket endpoint.
   *
   * @return {@code this} object.
   */
  public WebSocket clearUserInfo() {
    handshakeBuilder.clearUserInfo();
    return this;
  }


  /**
   * Connect to the server, send an opening handshake to the server, receive
   * the response and then start threads to communicate with the server.
   *
   * <p> As necessary, {@link #addProtocol(String)}, {@link
   * #addExtension(WebSocketExtension)} {@link #addHeader(String, String)}
   * should be called before you call this method. It is because the
   * parameters set by these methods are used in the opening handshake.
   *
   * <p>Also, as necessary, {@link #getSocket()} should be used to set up
   * socket parameters before you call this method. For example, you can set
   * the socket timeout like the following.<pre>
   * WebSocket websocket = ......;
   * websocket.{@link #getSocket() getSocket()}.{@link Socket#setSoTimeout(int)
   * setSoTimeout}(5000);
   * </pre>
   *
   * <p> If the WebSocket endpoint requires Basic Authentication, you can set
   * credentials by {@link #setUserInfo(String) setUserInfo(userInfo)} or
   * {@link #setUserInfo(String, String) setUserInfo(id, password)} before you
   * call this method. Note that if the URI passed to {@link WebSocketFactory}
   * {@code .createSocket} method contains the user-info part, you don't have
   * to call {@code setUserInfo} method.
   *
   * <p> Note that this method can be called at most only once regardless of
   * whether this method succeeded or failed. If you want to re-connect to the
   * WebSocket endpoint, you have to create a new {@code WebSocket} instance
   * again by calling one of {@code createSocket} methods of a {@link
   * WebSocketFactory}. You may find {@link #recreate()} method useful if you
   * want to create a new {@code WebSocket} instance that has the same
   * settings as this instance. (But settings you made on the raw socket are
   * not copied.)
   *
   * @return {@code this} object.
   * @throws WebSocketException The current state of the WebSocket is
   *                            not {@link WebSocketState#CREATED CREATED}. Connecting the
   *                            server failed. The opening handshake failed.
   */
  public WebSocket connect() throws WebSocketException {
    // Change the state to CONNECTING. If the state before
    // the change is not CREATED, an exception is thrown.
    changeStateOnConnect();

    // HTTP headers from the server.
    Map<String, List<String>> headers;

    try {
      // Connect to the server.
      socketConnector.connect();

      // Perform WebSocket handshake.
      headers = shakeHands();
    } catch (final WebSocketException e) {
      // Close the socket.
      socketConnector.closeSilently();

      // Change the state to CLOSED.
      stateManager.setState(CLOSED);

      // Notify the listener of the state change.
      listenerManager.callOnStateChanged(CLOSED);

      // The handshake failed.
      throw e;
    }

    // HTTP headers in the response from the server.
    serverHeaders = headers;

    // Extensions.
    perMessageCompressionExtension = findAgreedPerMessageCompressionExtension();

    // Change the state to OPEN.
    stateManager.setState(OPEN);

    // Notify the listener of the state change.
    listenerManager.callOnStateChanged(OPEN);

    // Start threads that communicate with the server.
    startThreads();

    return this;
  }


  /**
   * Execute {@link #connect()} asynchronously using the given {@link
   * ExecutorService}. This method is just an alias of the following:
   * <blockquote>
   * executorService.{@link ExecutorService#submit(Callable) submit}({@link #connectable()})
   * </blockquote>
   *
   * @param executorService An {@link ExecutorService} to execute a task
   *                        created by {@link #connectable()}.
   * @return The value returned from {@link ExecutorService#submit(Callable)}.
   * @throws NullPointerException       If the given {@link ExecutorService} is
   *                                    {@code null}.
   * @throws RejectedExecutionException If the given {@link ExecutorService}
   *                                    rejected the task created by {@link #connectable()}.
   * @see #connectAsynchronously()
   */
  public Future<WebSocket> connect(final ExecutorService executorService) {
    return executorService.submit(connectable());
  }


  /**
   * Get a new {@link Callable}{@code <}{@link WebSocket}{@code >} instance
   * whose {@link Callable#call() call()} method calls {@link #connect()}
   * method of this {@code WebSocket} instance.
   *
   * @return A new {@link Callable}{@code <}{@link WebSocket}{@code >}
   * instance for asynchronous {@link #connect()}.
   * @see #connect(ExecutorService)
   */
  public Callable<WebSocket> connectable() {
    return new Connectable(this);
  }


  /**
   * Execute {@link #connect()} asynchronously by creating a new thread and
   * calling {@code connect()} in the thread. If {@code connect()} failed,
   * {@link WebSocketListener#onConnectError(WebSocket, WebSocketException)
   * onConnectError()} method of {@link WebSocketListener} is called.
   *
   * @return {@code this} object.
   */
  public WebSocket connectAsynchronously() {
    final Thread thread = new ConnectThread(this);

    // Get the reference (just in case)
    final ListenerManager lm = listenerManager;

    if (lm != null) {
      lm.callOnThreadCreated(ThreadType.CONNECT_THREAD, thread);
    }

    thread.start();

    return this;
  }


  /**
   * Disconnect the WebSocket.
   *
   * <p> This method is an alias of {@link #disconnect(int, String) disconnect}
   * {@code (}{@link WebSocketCloseCode#NORMAL}{@code, null)}.
   *
   * @return {@code this} object.
   */
  public WebSocket disconnect() {
    return disconnect(WebSocketCloseCode.NORMAL, null);
  }


  /**
   * Disconnect the WebSocket.
   *
   * <p> This method is an alias of {@link #disconnect(int, String) disconnect}
   * {@code (closeCode, null)}.
   *
   * @param closeCode The close code embedded in a <a href=
   *                  "https://tools.ietf.org/html/rfc6455#section-5.5.1">close frame</a>
   *                  which this WebSocket client will send to the server.
   * @return {@code this} object.
   */
  public WebSocket disconnect(final int closeCode) {
    return disconnect(closeCode, null);
  }


  /**
   * Disconnect the WebSocket.
   *
   * <p> This method is an alias of {@link #disconnect(int, String, long)
   * disconnect}{@code (closeCode, reason, 10000L)}.
   *
   * @param closeCode The close code embedded in a <a href=
   *                  "https://tools.ietf.org/html/rfc6455#section-5.5.1">close frame</a>
   *                  which this WebSocket client will send to the server.
   * @param reason    The reason embedded in a <a href=
   *                  "https://tools.ietf.org/html/rfc6455#section-5.5.1">close frame</a>
   *                  which this WebSocket client will send to the server. Note that the
   *                  length of the bytes which represents the given reason must not
   *                  exceed 125. In other words, {@code (reason.}{@link
   *                  String#getBytes(String) getBytes}{@code ("UTF-8").length <= 125)}
   *                  must be true.
   * @return {@code this} object.
   * @see WebSocketCloseCode
   * @see <a href="https://tools.ietf.org/html/rfc6455#section-5.5.1">RFC 6455, 5.5.1. Close</a>
   */
  public WebSocket disconnect(final int closeCode, final String reason) {
    return disconnect(closeCode, reason, DEFAULT_CLOSE_DELAY);
  }


  /**
   * Disconnect the WebSocket.
   *
   * @param closeCode  The close code embedded in a <a href=
   *                   "https://tools.ietf.org/html/rfc6455#section-5.5.1">close frame</a>
   *                   which this WebSocket client will send to the server.
   * @param reason     The reason embedded in a <a href=
   *                   "https://tools.ietf.org/html/rfc6455#section-5.5.1">close frame</a>
   *                   which this WebSocket client will send to the server. Note that the
   *                   length of the bytes which represents the given reason must not
   *                   exceed 125. In other words, {@code (reason.}{@link
   *                   String#getBytes(String) getBytes}{@code ("UTF-8").length <= 125)}
   *                   must be true.
   * @param closeDelay Delay in milliseconds before calling {@link
   *                   Socket#close()} forcibly. This safeguard is needed for the case
   *                   where the server fails to send back a close frame. The default
   *                   value is 10000 (= 10 seconds). When a negative value is given, the
   *                   default value is used.<p>If a very short time (e.g. 0) is given,
   *                   it is likely to happen either (1) that this client will fail to
   *                   send a close frame to the server (in this case, you will probably
   *                   see an error message "Flushing frames to the server failed: Socket
   *                   closed") or (2) that the WebSocket connection will be closed
   *                   before this client receives a close frame from the server (in this
   *                   case, the second argument of {@link
   *                   WebSocketListener#onDisconnected(WebSocket, WebSocketFrame,
   *                   WebSocketFrame, boolean) WebSocketListener.onDisconnected} will be
   *                   {@code null}).
   * @return {@code this} object.
   * @see WebSocketCloseCode
   * @see <a href="https://tools.ietf.org/html/rfc6455#section-5.5.1">RFC 6455, 5.5.1. Close</a>
   */
  public WebSocket disconnect(final int closeCode, final String reason, long closeDelay) {
    synchronized (stateManager) {
      switch (stateManager.getState()) {
        case CREATED:
          finishAsynchronously();
          return this;

        case OPEN:
          break;

        default:
          // - CONNECTING
          //     It won't happen unless the programmer dare call
          //     open() and disconnect() in parallel.
          //
          // - CLOSING
          //     A closing handshake has already been started.
          //
          // - CLOSED
          //     The connection has already been closed.
          return this;
      }

      // Change the state to CLOSING.
      stateManager.changeToClosing(CloseInitiator.CLIENT);

      // Create a close frame.
      final WebSocketFrame frame = WebSocketFrame.createCloseFrame(closeCode, reason);

      // Send the close frame to the server.
      sendFrame(frame);
    }

    // Notify the listeners of the state change.
    listenerManager.callOnStateChanged(CLOSING);

    // If a negative value is given.
    if (closeDelay < 0) {
      // Use the default value.
      closeDelay = DEFAULT_CLOSE_DELAY;
    }

    // Request the threads to stop.
    stopThreads(closeDelay);

    return this;
  }


  /**
   * Disconnect the WebSocket.
   *
   * <p> This method is an alias of {@link #disconnect(int, String)
   * disconnect}{@code (}{@link WebSocketCloseCode#NORMAL}{@code , reason)}.
   *
   * @param reason The reason embedded in a <a href=
   *               "https://tools.ietf.org/html/rfc6455#section-5.5.1">close frame</a>
   *               which this WebSocket client will send to the server. Note that the
   *               length of the bytes which represents the given reason must not
   *               exceed 125. In other words, {@code (reason.}{@link
   *               String#getBytes(String) getBytes}{@code ("UTF-8").length <= 125)}
   *               must be true.
   * @return {@code this} object.
   */
  public WebSocket disconnect(final String reason) {
    return disconnect(WebSocketCloseCode.NORMAL, reason);
  }


  @Override
  protected void finalize() throws Throwable {
    if (isInState(CREATED)) {
      // The raw socket needs to be closed.
      finish();
    }

    super.finalize();
  }


  /**
   * Find a per-message compression extension from among the agreed extensions.
   */
  private PerMessageCompressionExtension findAgreedPerMessageCompressionExtension() {
    if (agreedExtensions == null) {
      return null;
    }

    for (final WebSocketExtension extension : agreedExtensions) {
      if (extension instanceof PerMessageCompressionExtension) {
        return (PerMessageCompressionExtension) extension;
      }
    }

    return null;
  }


  void finish() {
    // Stop the ping sender and the pong sender.
    pingSender.stop();
    pongSender.stop();

    try {
      // Close the raw socket.
      socketConnector.getSocket().close();
    } catch (final Throwable t) {
      // Ignore any error raised by close().
    }

    synchronized (stateManager) {
      // Change the state to CLOSED.
      stateManager.setState(CLOSED);
    }

    // Notify the listeners of the state change.
    listenerManager.callOnStateChanged(CLOSED);

    // Notify the listeners that the WebSocket was disconnected.
    listenerManager.callOnDisconnected(serverCloseFrame, clientCloseFrame, stateManager.getClosedByServer());
  }


  /**
   * Call {@link #finish()} from within a separate thread.
   */
  private void finishAsynchronously() {
    final WebSocketThread thread = new FinishThread(this);

    // Execute onThreadCreated() of the listeners.
    thread.callOnThreadCreated();

    thread.start();
  }


  /**
   * Flush frames to the server; Flush is performed asynchronously.
   *
   * @return {@code this} object.
   */
  public WebSocket flush() {
    synchronized (stateManager) {
      final WebSocketState state = stateManager.getState();

      if (state != OPEN && state != CLOSING) {
        return this;
      }
    }

    // Get the reference to the instance of WritingThread.
    final WritingThread wt = writeThread;

    // If and only if an instance of WritingThread is available.
    if (wt != null) {
      // Request flush.
      wt.queueFlush();
    }

    return this;
  }


  /**
   * Get the agreed extensions.
   *
   * <p> This method works correctly only after {@link #connect()} succeeds
   * (= after the opening handshake succeeds).
   *
   * @return The agreed extensions.
   */
  public List<WebSocketExtension> getAgreedExtensions() {
    return agreedExtensions;
  }

  /**
   * Set the agreed extensions. {@link HandshakeReader} uses this method.
   */
  void setAgreedExtensions(final List<WebSocketExtension> extensions) {
    agreedExtensions = extensions;
  }

  /**
   * Get the agreed protocol.
   *
   * <p>This method works correctly only after {@link #connect()} succeeds (=
   * after the opening handshake succeeds).
   *
   * @return The agreed protocol.
   */
  public String getAgreedProtocol() {
    return agreedProtocol;
  }

  /**
   * Set the agreed protocol. {@link HandshakeReader} uses this method.
   */
  void setAgreedProtocol(final String protocol) {
    agreedProtocol = protocol;
  }

  /**
   * Get the size of the frame queue.
   *
   * <p>The default value is 0 and it means there is no limit on the queue size.
   *
   * @return The size of the frame queue.
   */
  public int getFrameQueueSize() {
    return frameQueueSize;
  }

  /**
   * Set the size of the frame queue.
   *
   * <p>The default value is 0 and it means there is no limit on the queue
   * size.
   *
   * <p>{@code send<i>Xxx</i>} methods queue a {@link WebSocketFrame} instance
   * to the internal queue. If the number of frames in the queue has reached
   * the upper limit (which has been set by this method) when a {@code
   * send<i>Xxx</i>} method is called, the method blocks until the queue gets
   * spaces.
   *
   * <p> Under some conditions, even if the queue is full, {@code
   * send<i>Xxx</i>} methods do not block. For example, in the case where the
   * thread to send frames ({@code WritingThread}) is going to stop or has
   * already stopped. In addition, method calls to send a <a href=
   * "https://tools.ietf.org/html/rfc6455#section-5.5">control frame</a> (e.g.
   * {@link #sendClose()} and {@link #sendPing()}) do not block.
   *
   * @param size The queue size. 0 means no limit. Negative numbers are not allowed.
   * @return {@code this} object.
   * @throws IllegalArgumentException if {@code size} is negative.
   */
  public WebSocket setFrameQueueSize(final int size) throws IllegalArgumentException {
    if (size < 0) {
      throw new IllegalArgumentException("size must not be negative.");
    }
    frameQueueSize = size;
    return this;
  }

  /**
   * Get the handshake builder.
   *
   * <p>{@link HandshakeReader} uses this method.
   */
  HandshakeBuilder getHandshakeBuilder() {
    return handshakeBuilder;
  }

  /**
   * Get the input stream of the WebSocket connection.
   */
  WebSocketInputStream getInput() {
    return socketInput;
  }

  /**
   * Get the manager that manages registered listeners.
   */
  ListenerManager getListenerManager() {
    return listenerManager;
  }

  /**
   * Get the maximum payload size. The default value is 0 which means that the
   * maximum payload size is not set and as a result frames are not split.
   *
   * @return The maximum payload size. 0 means that the maximum payload size
   * is not set.
   */
  public int getMaxPayloadSize() {
    return maxPayloadSize;
  }

  /**
   * Set the maximum payload size.
   *
   * <p>Text, binary and continuation frames whose payload size is bigger than
   * the maximum payload size will be split into multiple frames. Note that
   * control frames (close, ping and pong frames) are not split as per the
   * specification even if their payload size exceeds the maximum payload
   * size.
   *
   * @param size The maximum payload size. 0 to unset the maximum payload
   *             size.
   * @return {@code this} object.
   * @throws IllegalArgumentException if {@code size} is negative.
   */
  public WebSocket setMaxPayloadSize(final int size) throws IllegalArgumentException {
    if (size < 0) {
      throw new IllegalArgumentException("size must not be negative.");
    }
    maxPayloadSize = size;
    return this;
  }

  /**
   * Get the output stream of the WebSocket connection.
   */
  WebSocketOutputStream getOutput() {
    return socketOutput;
  }

  /**
   * Get the PerMessageCompressionExtension in the agreed extensions.
   * This method returns null if a per-message compression extension
   * is not found in the agreed extensions.
   */
  PerMessageCompressionExtension getPerMessageCompressionExtension() {
    return perMessageCompressionExtension;
  }

  /**
   * Get the interval of periodical
   * <a href="https://tools.ietf.org/html/rfc6455#section-5.5.2">ping</a>
   * frames.
   *
   * @return The interval in milliseconds.
   */
  public long getPingInterval() {
    return pingSender.getInterval();
  }

  /**
   * Set the interval of periodical
   * <a href="https://tools.ietf.org/html/rfc6455#section-5.5.2">ping</a>
   * frames.
   *
   * <p>Setting a positive number starts sending ping frames periodically.
   * Setting zero stops the periodical sending. This method can be called
   * both before and after {@link #connect()} method.
   *
   * @param interval The interval in milliseconds. A negative value is
   *                 regarded as zero.
   * @return {@code this} object.
   */
  public WebSocket setPingInterval(final long interval) {
    pingSender.setInterval(interval);
    return this;
  }

  /**
   * Get the generator of payload of ping frames that are sent automatically.
   *
   * @return The generator of payload ping frames that are sent automatically.
   */
  public PayloadGenerator getPingPayloadGenerator() {
    return pingSender.getPayloadGenerator();
  }

  /**
   * Set the generator of payload of ping frames that are sent automatically.
   *
   * @param generator The generator of payload ping frames that are sent
   *                  automatically.
   * @return the websocket
   */
  public WebSocket setPingPayloadGenerator(final PayloadGenerator generator) {
    pingSender.setPayloadGenerator(generator);
    return this;
  }

  /**
   * Get the interval of periodical
   * <a href="https://tools.ietf.org/html/rfc6455#section-5.5.3">pong</a>
   * frames.
   *
   * @return The interval in milliseconds.
   */
  public long getPongInterval() {
    return pongSender.getInterval();
  }

  /**
   * Set the interval of periodical
   * <a href="https://tools.ietf.org/html/rfc6455#section-5.5.3">pong</a>
   * frames.
   *
   * <p>Setting a positive number starts sending pong frames periodically.
   * Setting zero stops the periodical sending. This method can be called
   * both before and after {@link #connect()} method.
   *
   * <blockquote>
   * <dl>
   * <dt>
   * <span style="font-weight: normal;">An excerpt from <a href=
   * "https://tools.ietf.org/html/rfc6455#section-5.5.3"
   * >RFC 6455, 5.5.3. Pong</a></span>
   * </dt>
   * <dd>
   * <p><i>A Pong frame MAY be sent <b>unsolicited</b>. This serves as a
   * unidirectional heartbeat.  A response to an unsolicited Pong
   * frame is not expected.
   * </i></p>
   * </dd>
   * </dl>
   * </blockquote>
   *
   * @param interval The interval in milliseconds. A negative value is
   *                 regarded as zero.
   * @return {@code this} object.
   */
  public WebSocket setPongInterval(final long interval) {
    pongSender.setInterval(interval);
    return this;
  }

  /**
   * Get the generator of payload of pong frames that are sent automatically.
   *
   * @return The generator of payload pong frames that are sent automatically.
   */
  public PayloadGenerator getPongPayloadGenerator() {
    return pongSender.getPayloadGenerator();
  }

  /**
   * Set the generator of payload of pong frames that are sent automatically.
   *
   * @param generator The generator of payload ppng frames that are sent
   *                  automatically.
   * @return the websocket
   */
  public WebSocket setPongPayloadGenerator(final PayloadGenerator generator) {
    pongSender.setPayloadGenerator(generator);
    return this;
  }

  /**
   * Get the raw socket which this WebSocket uses internally.
   *
   * @return The underlying {@link Socket} instance.
   */
  public Socket getSocket() {
    return socketConnector.getSocket();
  }

  /**
   * Get the current state of this WebSocket.
   *
   * <p> The initial state is {@link WebSocketState#CREATED CREATED}. When
   * {@link #connect()} is called, the state is changed to {@link
   * WebSocketState#CONNECTING CONNECTING}, and then to {@link
   * WebSocketState#OPEN OPEN} after a successful opening handshake. The state
   * is changed to {@link WebSocketState#CLOSING CLOSING} when a closing
   * handshake is started, and then to {@link WebSocketState#CLOSED CLOSED}
   * when the closing handshake finished.
   *
   * <p> See the description of {@link WebSocketState} for details.
   *
   * @return The current state.
   * @see WebSocketState
   */
  public WebSocketState getState() {
    synchronized (stateManager) {
      return stateManager.getState();
    }
  }

  /**
   * Get the manager that manages the state of this {@code WebSocket} instance.
   */
  StateManager getStateManager() {
    return stateManager;
  }

  /**
   * Get the URI of the WebSocket endpoint. The scheme part is either
   * {@code "ws"} or {@code "wss"}. The authority part is always empty.
   *
   * @return The URI of the WebSocket endpoint.
   */
  public URI getURI() {
    return handshakeBuilder.getURI();
  }

  /**
   * Check if flush is performed automatically after {@link
   * #sendFrame(WebSocketFrame)} is done. The default value is {@code true}.
   *
   * @return {@code true} if flush is performed automatically.
   */
  public boolean isAutoFlush() {
    return autoFlush;
  }

  /**
   * Enable or disable auto-flush of sent frames.
   *
   * @param auto {@code true} to enable auto-flush. {@code false} to disable
   *             it.
   * @return {@code this} object.
   */
  public WebSocket setAutoFlush(final boolean auto) {
    autoFlush = auto;
    return this;
  }

  /**
   * Check if extended use of WebSocket frames are allowed.
   *
   * <p> When extended use is allowed, values of RSV1/RSV2/RSV3 bits and
   * opcode of frames are not checked. On the other hand, if not allowed
   * (default), non-zero values for RSV1/RSV2/RSV3 bits and unknown opcodes
   * cause an error. In such a case, {@link
   * WebSocketListener#onFrameError(WebSocket, WebSocketException,
   * WebSocketFrame) onFrameError} method of listeners are called and the
   * WebSocket is eventually closed.
   *
   * @return {@code true} if extended use of WebSocket frames are allowed.
   */
  public boolean isExtended() {
    return mExtended;
  }

  /**
   * Allow or disallow extended use of WebSocket frames.
   *
   * @param extended {@code true} to allow extended use of WebSocket frames.
   * @return {@code this} object.
   */
  public WebSocket setExtended(final boolean extended) {
    mExtended = extended;
    return this;
  }

  /**
   * Check if the current state is equal to the specified state.
   */
  private boolean isInState(final WebSocketState state) {
    synchronized (stateManager) {
      return (stateManager.getState() == state);
    }
  }

  /**
   * Check if this instance allows the server to close the WebSocket
   * connection without sending a <a href=
   * "https://tools.ietf.org/html/rfc6455#section-5.5.1">close frame</a>
   * to this client. The default value is {@code true}.
   *
   * @return {@code true} if the configuration allows for the server to close
   * the WebSocket connection without sending a close frame to this
   * client. {@code false} if the configuration requires that an error
   * be reported via {@link WebSocketListener#onError(WebSocket,
   * WebSocketException) onError()} method and {@link
   * WebSocketListener#onFrameError(WebSocket, WebSocketException,
   * WebSocketFrame) onFrameError()} method of {@link
   * WebSocketListener}.
   */
  public boolean isMissingCloseFrameAllowed() {
    return missingCloseFrameAllowed;
  }

  /**
   * Set whether to allow the server to close the WebSocket connection
   * without sending a <a href=
   * "https://tools.ietf.org/html/rfc6455#section-5.5.1">close frame</a>
   * to this client.
   *
   * @param allowed {@code true} to allow the server to close the WebSocket
   *                connection without sending a close frame to this client. {@code
   *                false} to make this instance report an error when the end of the
   *                input stream of the WebSocket connection is reached before a close
   *                frame is read.
   * @return {@code this} object.
   */
  public WebSocket setMissingCloseFrameAllowed(final boolean allowed) {
    missingCloseFrameAllowed = allowed;
    return this;
  }

  /**
   * Check if the current state of this WebSocket is {@link WebSocketState#OPEN
   * OPEN}.
   *
   * @return {@code true} if the current state is OPEN.
   */
  public boolean isOpen() {
    return isInState(OPEN);
  }

  /**
   * Called by the reading thread as its last step.
   */
  void onReadingThreadFinished(final WebSocketFrame closeFrame) {
    synchronized (threadMutex) {
      readingThreadFinished = true;
      serverCloseFrame = closeFrame;

      if (writingThreadFinished == false) {
        // Wait for the writing thread to finish.
        return;
      }
    }

    // Both the reading thread and the writing thread have finished.
    onThreadsFinished();
  }

  /**
   * Called by the reading thread as its first step.
   */
  void onReadingThreadStarted() {
    boolean bothStarted = false;

    synchronized (threadMutex) {
      readingThreadStarted = true;

      if (writingThreadStarted) {
        // Both the reading thread and the writing thread have started.
        bothStarted = true;
      }
    }

    // Call onConnected() method of listeners if not called yet.
    callOnConnectedIfNotYet();

    // If both the reading thread and the writing thread have started.
    if (bothStarted) {
      onThreadsStarted();
    }
  }

  /**
   * Called when both the reading thread and the writing thread have finished.
   *
   * <p>This method is called in the context of either the reading thread or
   * the writing thread.
   */
  private void onThreadsFinished() {
    finish();
  }

  /**
   * Called when both the reading thread and the writing thread have started.
   *
   * <p>This method is called in the context of either the reading thread or
   * the writing thread.
   */
  private void onThreadsStarted() {
    // Start sending ping frames periodically.
    // If the interval is zero, this call does nothing.
    pingSender.start();

    // Likewise, start the pong sender.
    pongSender.start();
  }

  /**
   * Called by the writing thread as its last step.
   */
  void onWritingThreadFinished(final WebSocketFrame closeFrame) {
    synchronized (threadMutex) {
      writingThreadFinished = true;
      clientCloseFrame = closeFrame;

      if (readingThreadFinished == false) {
        // Wait for the reading thread to finish.
        return;
      }
    }

    // Both the reading thread and the writing thread have finished.
    onThreadsFinished();
  }

  /**
   * Called by the writing thread as its first step.
   */
  void onWritingThreadStarted() {
    boolean bothStarted = false;

    synchronized (threadMutex) {
      writingThreadStarted = true;

      if (readingThreadStarted) {
        // Both the reading thread and the writing thread have started.
        bothStarted = true;
      }
    }

    // Call onConnected() method of listeners if not called yet.
    callOnConnectedIfNotYet();

    // If both the reading thread and the writing thread have started.
    if (bothStarted) {
      onThreadsStarted();
    }
  }

  /**
   * Open the input stream of the WebSocket connection.
   *
   * <p>The stream is used by the reading thread.
   */
  private WebSocketInputStream openInputStream(final Socket socket) throws WebSocketException {
    try {
      // Get the input stream of the raw socket through which
      // this client receives data from the server.
      return new WebSocketInputStream(new BufferedInputStream(socket.getInputStream()));
    } catch (final IOException e) {
      // Failed to get the input stream of the raw socket.
      throw new WebSocketException(WebSocketError.SOCKET_INPUT_STREAM_FAILURE, "Failed to get the input stream of the raw socket: " + e.getMessage(), e);
    }
  }

  /**
   * Open the output stream of the WebSocket connection.
   *
   * <p>The stream is used by the writing thread.
   */
  private WebSocketOutputStream openOutputStream(final Socket socket) throws WebSocketException {
    try {
      // Get the output stream of the socket through which
      // this client sends data to the server.
      return new WebSocketOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    } catch (final IOException e) {
      // Failed to get the output stream from the raw socket.
      throw new WebSocketException(WebSocketError.SOCKET_OUTPUT_STREAM_FAILURE, "Failed to get the output stream from the raw socket: " + e.getMessage(), e);
    }
  }

  /**
   * Receive an opening handshake response from the WebSocket server.
   */
  private Map<String, List<String>> readHandshake(final WebSocketInputStream input, final String key) throws WebSocketException {
    return new HandshakeReader(this).readHandshake(input, key);
  }

  /**
   * Create a new {@code WebSocket} instance that has the same settings as this
   * instance. Note that, however, settings you made on the raw socket are not
   * copied.
   *
   * <p>The {@link WebSocketFactory} instance that you used to create this
   * {@code WebSocket} instance is used again.
   *
   * <p>This method calls {@link #recreate(int)} with the timeout value that
   * was used when this instance was created. If you want to create a socket
   * connection with a different timeout value, use {@link #recreate(int)}
   * method instead.
   *
   * @return A new {@code WebSocket} instance.
   * @throws IOException {@link WebSocketFactory#createSocket(URI)} threw an
   *                     exception.
   */
  public WebSocket recreate() throws IOException {
    return recreate(socketConnector.getConnectionTimeout());
  }

  /**
   * Create a new {@code WebSocket} instance that has the same settings
   * as this instance. Note that, however, settings you made on the raw
   * socket are not copied.
   *
   * <p> The {@link WebSocketFactory} instance that you used to create this
   * {@code WebSocket} instance is used again.
   *
   * @param timeout The timeout value in milliseconds for socket timeout. A
   *                timeout of zero is interpreted as an infinite timeout.
   * @return A new {@code WebSocket} instance.
   * @throws IllegalArgumentException The given timeout value is negative.
   * @throws IOException              {@link WebSocketFactory#createSocket(URI)} threw an
   *                                  exception.
   */
  public WebSocket recreate(final int timeout) throws IOException {
    if (timeout < 0) {
      throw new IllegalArgumentException("The given timeout value is negative.");
    }

    final WebSocket instance = webSocketFactory.createSocket(getURI(), timeout);

    // Copy settings
    instance.handshakeBuilder = new HandshakeBuilder(handshakeBuilder);
    instance.setPingInterval(getPingInterval());
    instance.setPongInterval(getPongInterval());
    instance.setPingPayloadGenerator(getPingPayloadGenerator());
    instance.setPongPayloadGenerator(getPongPayloadGenerator());
    instance.mExtended = mExtended;
    instance.autoFlush = autoFlush;
    instance.missingCloseFrameAllowed = missingCloseFrameAllowed;
    instance.frameQueueSize = frameQueueSize;

    // Copy listeners
    final List<WebSocketListener> listeners = listenerManager.getListeners();
    synchronized (listeners) {
      instance.addListeners(listeners);
    }

    return instance;
  }

  /**
   * Remove an extension from {@code Sec-WebSocket-Extension}.
   *
   * @param extension An extension to remove. {@code null} is silently ignored.
   * @return {@code this} object.
   */
  public WebSocket removeExtension(final WebSocketExtension extension) {
    handshakeBuilder.removeExtension(extension);
    return this;
  }

  /**
   * Remove extensions from {@code Sec-WebSocket-Extension} by
   * an extension name.
   *
   * @param name An extension name. {@code null} is silently ignored.
   * @return {@code this} object.
   */
  public WebSocket removeExtensions(final String name) {
    handshakeBuilder.removeExtensions(name);
    return this;
  }

  /**
   * Remove pairs of extra HTTP headers.
   *
   * @param name An HTTP header name. {@code null} is silently ignored.
   * @return {@code this} object.
   */
  public WebSocket removeHeaders(final String name) {
    handshakeBuilder.removeHeaders(name);
    return this;
  }

  /**
   * Remove a listener from this WebSocket.
   *
   * @param listener A listener to remove. {@code null} won't cause an error.
   * @return {@code this} object.
   */
  public WebSocket removeListener(final WebSocketListener listener) {
    listenerManager.removeListener(listener);
    return this;
  }

  /**
   * Remove listeners.
   *
   * @param listeners Listeners to remove. {@code null} is silently ignored.
   *                  {@code null} elements in the list are ignored, too.
   * @return {@code this} object.
   */
  public WebSocket removeListeners(final List<WebSocketListener> listeners) {
    listenerManager.removeListeners(listeners);
    return this;
  }

  /**
   * Remove a protocol from {@code Sec-WebSocket-Protocol}.
   *
   * @param protocol A protocol name. {@code null} is silently ignored.
   * @return {@code this} object.
   */
  public WebSocket removeProtocol(final String protocol) {
    handshakeBuilder.removeProtocol(protocol);
    return this;
  }

  /**
   * Send a binary message to the server.
   *
   * <p>This method is an alias of {@link #sendFrame(WebSocketFrame)
   * sendFrame}{@code (WebSocketFrame.}{@link
   * WebSocketFrame#createBinaryFrame(byte[])
   * createBinaryFrame}{@code (message))}.
   *
   * <p> If you want to send a binary frame that is to be followed by
   * continuation frames, use {@link #sendBinary(byte[], boolean)
   * setBinary(byte[] payload, boolean fin)} with {@code fin=false}.
   *
   * @param message A binary message to be sent to the server.
   * @return {@code this} object.
   */
  public WebSocket sendBinary(final byte[] message) {
    return sendFrame(WebSocketFrame.createBinaryFrame(message));
  }

  /**
   * Send a binary frame to the server.
   *
   * <p>This method is an alias of {@link #sendFrame(WebSocketFrame) sendFrame}
   * {@code (WebSocketFrame.}{@link WebSocketFrame#createBinaryFrame(byte[])
   * createBinaryFrame}{@code (payload).}{@link WebSocketFrame#setFin(boolean)
   * setFin}{@code (fin))}.
   *
   * @param payload The payload of a binary frame.
   * @param fin     The FIN bit value.
   * @return {@code this} object.
   */
  public WebSocket sendBinary(final byte[] payload, final boolean fin) {
    return sendFrame(WebSocketFrame.createBinaryFrame(payload).setFin(fin));
  }

  /**
   * Send a close frame to the server.
   *
   * <p>This method is an alias of {@link #sendFrame(WebSocketFrame)
   * sendFrame}{@code (WebSocketFrame.}{@link
   * WebSocketFrame#createCloseFrame() createCloseFrame()}).
   *
   * @return {@code this} object.
   */
  public WebSocket sendClose() {
    return sendFrame(WebSocketFrame.createCloseFrame());
  }

  /**
   * Send a close frame to the server.
   *
   * <p>This method is an alias of {@link #sendFrame(WebSocketFrame)
   * sendFrame}{@code (WebSocketFrame.}{@link
   * WebSocketFrame#createCloseFrame(int)
   * createCloseFrame}{@code (closeCode))}.
   *
   * @param closeCode The close code.
   * @return {@code this} object.
   * @see WebSocketCloseCode
   */
  public WebSocket sendClose(final int closeCode) {
    return sendFrame(WebSocketFrame.createCloseFrame(closeCode));
  }

  /**
   * Send a close frame to the server.
   *
   * <p>This method is an alias of {@link #sendFrame(WebSocketFrame)
   * sendFrame}{@code (WebSocketFrame.}{@link
   * WebSocketFrame#createCloseFrame(int, String)
   * createCloseFrame}{@code (closeCode, reason))}.
   *
   * @param closeCode The close code.
   * @param reason    The close reason. Note that a control frame's payload
   *                  length must be 125 bytes or less (RFC 6455,
   *                  <a href="https://tools.ietf.org/html/rfc6455#section-5.5">5.5.
   *                  Control Frames</a>).
   * @return {@code this} object.
   * @see WebSocketCloseCode
   */
  public WebSocket sendClose(final int closeCode, final String reason) {
    return sendFrame(WebSocketFrame.createCloseFrame(closeCode, reason));
  }

  /**
   * Send a continuation frame to the server.
   *
   * <p>This method is an alias of {@link #sendFrame(WebSocketFrame)
   * sendFrame}{@code (WebSocketFrame.}{@link
   * WebSocketFrame#createContinuationFrame()
   * createContinuationFrame()}{@code )}.
   *
   * <p>Note that the FIN bit of a frame sent by this method is {@code false}.
   * If you want to set the FIN bit, use {@link #sendContinuation(boolean)
   * sendContinuation(boolean fin)} with {@code fin=true}.
   *
   * @return {@code this} object.
   */
  public WebSocket sendContinuation() {
    return sendFrame(WebSocketFrame.createContinuationFrame());
  }

  /**
   * Send a continuation frame to the server.
   *
   * <p> This method is an alias of {@link #sendFrame(WebSocketFrame)
   * sendFrame}{@code (WebSocketFrame.}{@link
   * WebSocketFrame#createContinuationFrame()
   * createContinuationFrame()}{@code .}{@link
   * WebSocketFrame#setFin(boolean) setFin}{@code (fin))}.
   *
   * @param fin The FIN bit value.
   * @return {@code this} object.
   */
  public WebSocket sendContinuation(final boolean fin) {
    return sendFrame(WebSocketFrame.createContinuationFrame().setFin(fin));
  }

  /**
   * Send a continuation frame to the server.
   *
   * <p>This method is an alias of {@link #sendFrame(WebSocketFrame)
   * sendFrame}{@code (WebSocketFrame.}{@link
   * WebSocketFrame#createContinuationFrame(byte[])
   * createContinuationFrame}{@code (payload))}.
   *
   * <p>Note that the FIN bit of a frame sent by this method is {@code false}.
   * If you want to set the FIN bit, use {@link #sendContinuation(byte[],
   * boolean) sendContinuation(byte[] payload, boolean fin)} with {@code
   * fin=true}.
   *
   * @param payload The payload of a continuation frame.
   * @return {@code this} object.
   */
  public WebSocket sendContinuation(final byte[] payload) {
    return sendFrame(WebSocketFrame.createContinuationFrame(payload));
  }

  /**
   * Send a continuation frame to the server.
   *
   * <p> This method is an alias of {@link #sendFrame(WebSocketFrame)
   * sendFrame}{@code (WebSocketFrame.}{@link
   * WebSocketFrame#createContinuationFrame(byte[])
   * createContinuationFrame}{@code (payload).}{@link
   * WebSocketFrame#setFin(boolean) setFin}{@code (fin))}.
   *
   * @param payload The payload of a continuation frame.
   * @param fin     The FIN bit value.
   * @return {@code this} object.
   */
  public WebSocket sendContinuation(final byte[] payload, final boolean fin) {
    return sendFrame(WebSocketFrame.createContinuationFrame(payload).setFin(fin));
  }

  /**
   * Send a continuation frame to the server.
   *
   * <p>This method is an alias of {@link #sendFrame(WebSocketFrame)
   * sendFrame}{@code (WebSocketFrame.}{@link
   * WebSocketFrame#createContinuationFrame(String)
   * createContinuationFrame}{@code (payload))}.
   *
   * <p>Note that the FIN bit of a frame sent by this method is {@code false}.
   * If you want to set the FIN bit, use {@link #sendContinuation(String,
   * boolean) sendContinuation(String payload, boolean fin)} with {@code
   * fin=true}.
   *
   * @param payload The payload of a continuation frame.
   * @return {@code this} object.
   */
  public WebSocket sendContinuation(final String payload) {
    return sendFrame(WebSocketFrame.createContinuationFrame(payload));
  }

  /**
   * Send a continuation frame to the server.
   *
   * <p>This method is an alias of {@link #sendFrame(WebSocketFrame)
   * sendFrame}{@code (WebSocketFrame.}{@link
   * WebSocketFrame#createContinuationFrame(String)
   * createContinuationFrame}{@code (payload).}{@link
   * WebSocketFrame#setFin(boolean) setFin}{@code (fin))}.
   *
   * @param payload The payload of a continuation frame.
   * @param fin     The FIN bit value.
   * @return {@code this} object.
   */
  public WebSocket sendContinuation(final String payload, final boolean fin) {
    return sendFrame(WebSocketFrame.createContinuationFrame(payload).setFin(fin));
  }

  /**
   * Send a WebSocket frame to the server.
   *
   * <p>This method just queues the given frame. Actual transmission
   * is performed asynchronously.
   *
   * <p>When the current state of this WebSocket is not {@link
   * WebSocketState#OPEN OPEN}, this method does not accept
   * the frame.
   *
   * <p>Sending a <a href="https://tools.ietf.org/html/rfc6455#section-5.5.1"
   * >close frame</a> changes the state to {@link WebSocketState#CLOSING
   * CLOSING} (if the current state is neither {@link WebSocketState#CLOSING
   * CLOSING} nor {@link WebSocketState#CLOSED CLOSED}).
   *
   * <p>Note that the validity of the give frame is not checked.
   * For example, even if the payload length of a given frame
   * is greater than 125 and the opcode indicates that the
   * frame is a control frame, this method accepts the given
   * frame.
   *
   * @param frame A WebSocket frame to be sent to the server. If {@code null}
   *              is given, nothing is done.
   * @return {@code this} object.
   */
  public WebSocket sendFrame(final WebSocketFrame frame) {
    if (frame == null) {
      return this;
    }

    synchronized (stateManager) {
      final WebSocketState state = stateManager.getState();

      if (state != OPEN && state != CLOSING) {
        return this;
      }
    }

    // The current state is either OPEN or CLOSING. Or, CLOSED.

    // Get the reference to the writing thread.
    final WritingThread wt = writeThread;

    // Some applications call sendFrame() without waiting for the
    // notification of WebSocketListener.onConnected() (Issue #23),
    // and/or even after the connection is closed. That is, there
    // are chances that sendFrame() is called when mWritingThread
    // is null. So, it should be checked whether an instance of
    // WritingThread is available or not before calling queueFrame().
    if (wt == null) {
      // An instance of WritingThread is not available.
      return this;
    }

    // Split the frame into multiple frames if necessary.
    final List<WebSocketFrame> frames = splitIfNecessary(frame);

    // Queue the frame or the frames. Even if the current state is
    // CLOSED, queueing won't be a big issue.

    // If the frame was not split.
    if (frames == null) {
      // Queue the frame.
      wt.queueFrame(frame);
    } else {
      for (final WebSocketFrame f : frames) {
        // Queue the frame.
        wt.queueFrame(f);
      }
    }

    return this;
  }

  /**
   * Send a ping frame to the server.
   *
   * <p>This method is an alias of {@link #sendFrame(WebSocketFrame)
   * sendFrame}{@code (WebSocketFrame.}{@link
   * WebSocketFrame#createPingFrame() createPingFrame()}).
   *
   * @return {@code this} object.
   */
  public WebSocket sendPing() {
    return sendFrame(WebSocketFrame.createPingFrame());
  }

  /**
   * Send a ping frame to the server.
   *
   * <p>This method is an alias of {@link #sendFrame(WebSocketFrame)
   * sendFrame}{@code (WebSocketFrame.}{@link
   * WebSocketFrame#createPingFrame(byte[])
   * createPingFrame}{@code (payload))}.
   *
   * @param payload The payload for a ping frame. Note that a control frames
   *                payload length must be 125 bytes or less (RFC 6455,
   *                <a href="https://tools.ietf.org/html/rfc6455#section-5.5">5.5.
   *                Control Frames</a>).
   * @return {@code this} object.
   */
  public WebSocket sendPing(final byte[] payload) {
    return sendFrame(WebSocketFrame.createPingFrame(payload));
  }

  /**
   * Send a ping frame to the server.
   *
   * <p>This method is an alias of {@link #sendFrame(WebSocketFrame)
   * sendFrame}{@code (WebSocketFrame.}{@link
   * WebSocketFrame#createPingFrame(String)
   * createPingFrame}{@code (payload))}.
   *
   * @param payload The payload for a ping frame. Note that a control frames
   *                payload length must be 125 bytes or less (RFC 6455,
   *                <a href="https://tools.ietf.org/html/rfc6455#section-5.5">5.5.
   *                Control Frames</a>).
   * @return {@code this} object.
   */
  public WebSocket sendPing(final String payload) {
    return sendFrame(WebSocketFrame.createPingFrame(payload));
  }

  /**
   * Send a pong frame to the server.
   *
   * <p>This method is an alias of {@link #sendFrame(WebSocketFrame)
   * sendFrame}{@code (WebSocketFrame.}{@link
   * WebSocketFrame#createPongFrame() createPongFrame()}).
   *
   * @return {@code this} object.
   */
  public WebSocket sendPong() {
    return sendFrame(WebSocketFrame.createPongFrame());
  }

  /**
   * Send a pong frame to the server.
   *
   * <p>This method is an alias of {@link #sendFrame(WebSocketFrame)
   * sendFrame}{@code (WebSocketFrame.}{@link
   * WebSocketFrame#createPongFrame(byte[])
   * createPongFrame}{@code (payload))}.
   *
   * @param payload The payload for a pong frame. Note that a control frames
   *                payload length must be 125 bytes or less (RFC 6455,
   *                <a href="https://tools.ietf.org/html/rfc6455#section-5.5">5.5.
   *                Control Frames</a>).
   * @return {@code this} object.
   */
  public WebSocket sendPong(final byte[] payload) {
    return sendFrame(WebSocketFrame.createPongFrame(payload));
  }

  /**
   * Send a pong frame to the server.
   *
   * <p>This method is an alias of {@link #sendFrame(WebSocketFrame)
   * sendFrame}{@code (WebSocketFrame.}{@link
   * WebSocketFrame#createPongFrame(String)
   * createPongFrame}{@code (payload))}.
   *
   * @param payload The payload for a pong frame. Note that a control frames
   *                payload length must be 125 bytes or less (RFC 6455,
   *                <a href="https://tools.ietf.org/html/rfc6455#section-5.5">5.5.
   *                Control Frames</a>).
   * @return {@code this} object.
   */
  public WebSocket sendPong(final String payload) {
    return sendFrame(WebSocketFrame.createPongFrame(payload));
  }

  /**
   * Send a text message to the server.
   *
   * <p>This method is an alias of {@link #sendFrame(WebSocketFrame)
   * sendFrame}{@code (WebSocketFrame.}{@link
   * WebSocketFrame#createTextFrame(String)
   * createTextFrame}{@code (message))}.
   *
   * <p>If you want to send a text frame that is to be followed by
   * continuation frames, use {@link #sendText(String, boolean)
   * setText(String payload, boolean fin)} with {@code fin=false}.
   *
   * @param message A text message to be sent to the server.
   * @return {@code this} object.
   */
  public WebSocket sendText(final String message) {
    return sendFrame(WebSocketFrame.createTextFrame(message));
  }

  /**
   * Send a text frame to the server.
   *
   * <p>This method is an alias of {@link #sendFrame(WebSocketFrame)
   * sendFrame}{@code (WebSocketFrame.}{@link
   * WebSocketFrame#createTextFrame(String)
   * createTextFrame}{@code (payload).}{@link
   * WebSocketFrame#setFin(boolean) setFin}{@code (fin))}.
   *
   * @param payload The payload of a text frame.
   * @param fin     The FIN bit value.
   * @return {@code this} object.
   */
  public WebSocket sendText(final String payload, final boolean fin) {
    return sendFrame(WebSocketFrame.createTextFrame(payload).setFin(fin));
  }

  /**
   * Set the credentials to connect to the WebSocket endpoint.
   *
   * @param userInfo The credentials for Basic Authentication. The format
   *                 should be {@code <i>id</i>:<i>password</i>}.
   * @return {@code this} object.
   */
  public WebSocket setUserInfo(final String userInfo) {
    handshakeBuilder.setUserInfo(userInfo);
    return this;
  }


  /**
   * Set the credentials to connect to the WebSocket endpoint.
   *
   * @param id       The ID.
   * @param password The password.
   * @return {@code this} object.
   */
  public WebSocket setUserInfo(final String id, final String password) {
    handshakeBuilder.setUserInfo(id, password);
    return this;
  }


  /**
   * Perform the opening handshake.
   */
  private Map<String, List<String>> shakeHands() throws WebSocketException {
    // The raw socket created by WebSocketFactory.
    final Socket socket = socketConnector.getSocket();

    // Get the input stream of the socket.
    final WebSocketInputStream input = openInputStream(socket);

    // Get the output stream of the socket.
    final WebSocketOutputStream output = openOutputStream(socket);

    // Generate a value for Sec-WebSocket-Key.
    final String key = generateWebSocketKey();

    // Send an opening handshake to the server.
    writeHandshake(output, key);

    // Read the response from the server.
    final Map<String, List<String>> headers = readHandshake(input, key);

    // Keep the input stream and the output stream to pass them
    // to the reading thread and the writing thread later.
    socketInput = input;
    socketOutput = output;

    // The handshake succeeded.
    return headers;
  }


  private List<WebSocketFrame> splitIfNecessary(final WebSocketFrame frame) {
    return WebSocketFrame.splitIfNecessary(frame, maxPayloadSize, perMessageCompressionExtension);
  }


  /**
   * Start both the reading thread and the writing thread.
   *
   * <p>The reading thread will call {@link #onReadingThreadStarted()} as its
   * first step. Likewise, the writing thread will call {@link
   * #onWritingThreadStarted()} as its first step. After both the threads have
   * started, {@link #onThreadsStarted()} is called.
   */
  private void startThreads() {
    final ReadingThread readingThread = new ReadingThread(this);
    final WritingThread writingThread = new WritingThread(this);

    synchronized (threadMutex) {
      readThread = readingThread;
      writeThread = writingThread;
    }

    // Execute onThreadCreated of the listeners.
    readingThread.callOnThreadCreated();
    writingThread.callOnThreadCreated();

    readingThread.start();
    writingThread.start();
  }


  /**
   * Stop both the reading thread and the writing thread.
   *
   * <p>The reading thread will call {@link
   * #onReadingThreadFinished(WebSocketFrame)} as its last step. Likewise, the
   * writing thread will call {@link #onWritingThreadFinished(WebSocketFrame)}
   * as its last step. After both the threads have stopped, {@link
   * #onThreadsFinished()} is called.
   */
  private void stopThreads(final long closeDelay) {
    ReadingThread readingThread;
    WritingThread writingThread;

    synchronized (threadMutex) {
      readingThread = readThread;
      writingThread = writeThread;

      readThread = null;
      writeThread = null;
    }

    if (readingThread != null) {
      readingThread.requestStop(closeDelay);
    }

    if (writingThread != null) {
      writingThread.requestStop();
    }
  }


  /**
   * Send an opening handshake request to the WebSocket server.
   */
  private void writeHandshake(final WebSocketOutputStream output, final String key) throws WebSocketException {
    // Generate an opening handshake sent to the server from this client.
    handshakeBuilder.setKey(key);
    final String requestLine = handshakeBuilder.buildRequestLine();
    final List<String[]> headers = handshakeBuilder.buildHeaders();
    final String handshake = HandshakeBuilder.build(requestLine, headers);

    // Call onSendingHandshake() method of listeners.
    listenerManager.callOnSendingHandshake(requestLine, headers);

    try {
      // Send the opening handshake to the server.
      output.write(handshake);
      output.flush();
    } catch (final IOException e) {
      // Failed to send an opening handshake request to the server.
      throw new WebSocketException(WebSocketError.OPENING_HAHDSHAKE_REQUEST_FAILURE, "Failed to send an opening handshake request to the server: " + e.getMessage(), e);
    }
  }

}
