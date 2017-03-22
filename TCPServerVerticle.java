import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetSocket;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

public class TCPServerVerticle extends AbstractVerticle {
	
	private Logger logger;
	private List<NetSocket> sockets;
	
	@Override
	public void start() {
		logger = LoggerFactory.getLogger(TCPServerVerticle.class);
		sockets = new ArrayList<NetSocket>();
		
		vertx.createNetServer().connectHandler(socket -> {
			sockets.add(socket); // add a new client to chat room user list
			logger.info("["+socket.writeHandlerID()+"] has joined chat room.");
			for(NetSocket s : sockets) s.write("["+socket.writeHandlerID()+"] has joined chat room.\n");
			
			socket.handler(buffer -> {
				// broadcast message to all clients except speaker
				speakToAllExceptMe(socket, "["+socket.writeHandlerID()+"] : " + buffer.toString());
				
			}); // end of (data) handler
			socket.closeHandler(v -> {
				logger.info("["+socket.writeHandlerID()+"] has quit chat room.");
				for(NetSocket s : sockets) s.write("["+socket.writeHandlerID()+"] has quit chat room.\n");
			});
			socket.exceptionHandler(throwable -> logger.error("unexpected exception : " + throwable));
		}).listen(9999, result -> logger.info("bind result : " + result.succeeded())); // end of createNetServer()
	} // end of start()
	
	private void speakToAllExceptMe(NetSocket me, String msg) {
		for(NetSocket s : sockets) {
			if(!me.equals(s)) s.write(msg);
		}
	}
}
