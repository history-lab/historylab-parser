package edu.columbia.historylab.ner.handlers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mysql.jdbc.Driver;

import edu.columbia.historylab.ner.constants.Constants;

public class DatabaseHandler {
	
	/*
	 * Returns documents given their IDs.
	 */
	public static Map<String, String> getDocuments(List<String> documentIds) throws SQLException{
		
		Map<String, String> documents = new HashMap<String, String>();

		//Connect to DB
		DriverManager.registerDriver(new Driver());
		Connection conn = DriverManager.getConnection(Config.getInstance().getDatabaseUrl()+Config.getInstance().getDatabaseName(), Config.getInstance().getDatabaseUsername(), Config.getInstance().getDatabasePassword());
		
		//Generate the USER select query
		String query = Constants.DB_QUERY_SELECT;
		for(int i=0; i<documentIds.size(); i++){
			query+="id='"+documentIds.get(i)+"'";
			if(i<documentIds.size()-1){
				query+=" OR ";
			}
		}
		
		System.out.println(query);
		Statement st = conn.createStatement();
      
		//Execute the query
		ResultSet rs = st.executeQuery(query);
		while (rs.next()){
			String documentId = rs.getString("id");
			String body = rs.getString("body");
			documents.put(documentId,  body);
		}
		//Close the statement and the connection
		st.close();
		conn.close();
		
		return documents;
	}
}
