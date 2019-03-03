package Commands;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import Model.Comment;

public class RetrieveComment extends Command {

	@SuppressWarnings("unused")
	@Override
    public void execute() {
        HashMap < String, Object > props = parameters;
        Channel channel = (Channel) props.get("channel");
        JSONParser parser = new JSONParser();

        try {
			JSONObject messageBody = (JSONObject) parser.parse((String) props.get("body"));
			String uri = (messageBody).get("uri").toString();
			
			String target_id = "", type = "";
			if(uri.contains("?type=")) {
				target_id = StringUtils.substringBetween(uri, "/comment/", "?type=");
				type = ((JSONObject) (messageBody).get("parameters")).get("type").toString();
			}else {
				target_id = StringUtils.removeStart(uri, "/comment/");
			}

            AMQP.BasicProperties properties = (AMQP.BasicProperties) props.get("properties");
            AMQP.BasicProperties replyProps = (AMQP.BasicProperties) props.get("replyProps");
            Envelope envelope = (Envelope) props.get("envelope");
			
			ArrayList<HashMap<String, Object>> comments = Comment.get(target_id, type);
            JSONObject response = Command.jsonFromArray(comments, "comments");
            channel.basicPublish("", properties.getReplyTo(), replyProps, response.toString().getBytes("UTF-8"));
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }

}
