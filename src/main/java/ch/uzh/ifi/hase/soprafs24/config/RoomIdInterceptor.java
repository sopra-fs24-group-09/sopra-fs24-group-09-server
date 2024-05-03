// package ch.uzh.ifi.hase.soprafs24.config;
// import org.springframework.http.server.ServerHttpRequest;
// import org.springframework.http.server.ServerHttpResponse;
// import org.springframework.web.socket.WebSocketHandler;
// import org.springframework.web.socket.server.HandshakeInterceptor;

// import java.util.Map;

// public class RoomIdInterceptor implements HandshakeInterceptor {
//     @Override
//     public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
//         String path = request.getURI().getPath();
//         String[] pathSegments = path.split("/");
//         String roomId = pathSegments[2]; 
//         attributes.put("roomId", roomId);  // 存储 roomId 到session里
//         System.out.println("[WS Interceptor] roomId: " + roomId);
//         return true;
//     }

//     @Override
//     public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
//         // Optionally handle after handshake logic here
//         System.out.println("[WS Interceptor] afterHandshake");
//         System.out.println("\tRequest: " + request);
//         System.out.println("\tResponse: " + response);
//         System.out.println("\tWebSocketHandler: " + wsHandler);
//         System.out.println("\tException: " + exception);
//     }
// }
